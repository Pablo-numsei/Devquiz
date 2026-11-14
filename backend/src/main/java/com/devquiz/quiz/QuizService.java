package com.devquiz.quiz;

import com.devquiz.user.Usuario;
import com.devquiz.user.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class QuizService {
    private static final int QUANTIDADE_PADRAO = 10;

    private final CategoriaRepository categorias;
    private final QuestaoRepository questoes;
    private final TentativaRepository tentativas;
    private final TentativaQuestaoRepository tentativaQuestoes;
    private final RespostaRepository respostas;
    private final UsuarioRepository usuarios;

    public QuizService(
            CategoriaRepository categorias,
            QuestaoRepository questoes,
            TentativaRepository tentativas,
            TentativaQuestaoRepository tentativaQuestoes,
            RespostaRepository respostas,
            UsuarioRepository usuarios) {
        this.categorias = categorias;
        this.questoes = questoes;
        this.tentativas = tentativas;
        this.tentativaQuestoes = tentativaQuestoes;
        this.respostas = respostas;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarCategorias() {
        return categorias.findByAtivaTrueOrderByNomeAsc().stream()
                .map(c -> new CategoriaResponse(c.getId(), c.getNome(), c.getDescricao()))
                .toList();
    }

    @Transactional
    public InicioResponse iniciar(String apelido, List<Integer> categoriaIds, String dificuldade, Integer quantidadeInformada) {
        if (categoriaIds == null || categoriaIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione pelo menos uma categoria.");
        }

        String nivel = normalizarDificuldade(dificuldade);
        int quantidade = quantidadeInformada == null ? QUANTIDADE_PADRAO : quantidadeInformada;
        if (quantidade < 1 || quantidade > QUANTIDADE_PADRAO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A quantidade deve estar entre 1 e 10.");
        }

        List<Integer> idsUnicos = categoriaIds.stream().filter(Objects::nonNull).distinct().toList();
        if (idsUnicos.isEmpty() || categorias.findAllById(idsUnicos).stream().filter(c -> Boolean.TRUE.equals(c.getAtiva())).count() != idsUnicos.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uma ou mais categorias são inválidas.");
        }

        List<Questao> disponiveis = new ArrayList<>(
                questoes.findByCategoria_IdInAndDificuldadeAndAtivaTrue(idsUnicos, nivel));
        if (disponiveis.size() < quantidade) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ainda não existem questões suficientes para essa seleção. Disponíveis: " + disponiveis.size() + ".");
        }

        Collections.shuffle(disponiveis);
        List<Questao> selecionadas = disponiveis.subList(0, quantidade);

        Usuario usuario = usuarios.findByApelidoIgnoreCase(apelido)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado."));

        Tentativa tentativa = tentativas.save(new Tentativa(usuario, nivel, quantidade));
        tentativaQuestoes.saveAll(selecionadas.stream()
                .map(q -> new TentativaQuestao(tentativa, q))
                .toList());

        List<PerguntaResponse> perguntas = selecionadas.stream()
                .map(q -> new PerguntaResponse(
                        q.getId(),
                        q.getCategoria().getNome(),
                        q.getEnunciado(),
                        q.getCodigo(),
                        q.getAlternativas().stream()
                                .map(a -> new AlternativaResponse(a.getId(), a.getTexto()))
                                .toList()))
                .toList();

        return new InicioResponse(tentativa.getId(), nivel, perguntas);
    }

    @Transactional
    public ResultadoResponse finalizar(String apelido, Long tentativaId, List<RespostaRequest> respostasRecebidas) {
        Usuario usuario = usuarios.findByApelidoIgnoreCase(apelido)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado."));

        Tentativa tentativa = tentativas.findByIdAndUsuario_Id(tentativaId, usuario.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tentativa não encontrada."));

        if (!"EM_ANDAMENTO".equals(tentativa.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta tentativa já foi finalizada.");
        }

        List<TentativaQuestao> itens = tentativaQuestoes.findByTentativa_Id(tentativaId);
        if (itens.size() != tentativa.getTotalQuestoes()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A tentativa está inconsistente. Inicie outra.");
        }

        Map<Long, RespostaRequest> enviadas = new HashMap<>();
        if (respostasRecebidas != null) {
            for (RespostaRequest resposta : respostasRecebidas) {
                if (resposta == null || resposta.questaoId() == null || enviadas.putIfAbsent(resposta.questaoId(), resposta) != null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Há respostas duplicadas ou inválidas.");
                }
            }
        }

        Set<Long> questoesDaTentativa = itens.stream()
                .map(item -> item.getQuestao().getId())
                .collect(Collectors.toSet());
        if (!questoesDaTentativa.containsAll(enviadas.keySet())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A resposta contém uma questão que não pertence a esta tentativa.");
        }

        int acertos = 0;
        int pontosPorAcerto = pontosPorDificuldade(tentativa.getDificuldade());
        List<Resposta> paraSalvar = new ArrayList<>();
        List<DetalheResultado> detalhes = new ArrayList<>();

        for (TentativaQuestao item : itens) {
            Questao questao = item.getQuestao();
            RespostaRequest enviada = enviadas.get(questao.getId());
            Alternativa alternativa = null;

            if (enviada != null && enviada.alternativaId() != null) {
                alternativa = questao.getAlternativas().stream()
                        .filter(a -> a.getId().equals(enviada.alternativaId()))
                        .findFirst()
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "A alternativa não pertence à questão informada."));
            }

            boolean correta = alternativa != null && Boolean.TRUE.equals(alternativa.getCorreta());
            if (correta) acertos++;

            paraSalvar.add(new Resposta(tentativa, questao, alternativa, correta));

            Alternativa corretaDaQuestao = questao.getAlternativas().stream()
                    .filter(a -> Boolean.TRUE.equals(a.getCorreta()))
                    .findFirst()
                    .orElse(null);

            detalhes.add(new DetalheResultado(
                    questao.getId(),
                    alternativa == null ? null : alternativa.getId(),
                    correta,
                    corretaDaQuestao == null ? null : corretaDaQuestao.getId(),
                    corretaDaQuestao == null ? null : corretaDaQuestao.getTexto(),
                    questao.getExplicacao()));
        }

        int pontuacao = acertos * pontosPorAcerto;
        respostas.saveAll(paraSalvar);
        tentativa.finalizar(acertos, pontuacao);
        tentativas.save(tentativa);

        return new ResultadoResponse(
                tentativa.getId(),
                tentativa.getTotalQuestoes(),
                acertos,
                pontuacao,
                pontosPorAcerto,
                detalhes);
    }

    private String normalizarDificuldade(String dificuldade) {
        if (dificuldade == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a dificuldade.");
        }
        return switch (dificuldade.trim()) {
            case "Iniciante", "Intermediário", "Avançado" -> dificuldade.trim();
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dificuldade inválida.");
        };
    }

    private int pontosPorDificuldade(String dificuldade) {
        return switch (dificuldade) {
            case "Iniciante" -> 10;
            case "Intermediário" -> 20;
            case "Avançado" -> 30;
            default -> throw new IllegalStateException("Dificuldade não reconhecida.");
        };
    }

    public record CategoriaResponse(Integer id, String nome, String descricao) {}
    public record AlternativaResponse(Long id, String texto) {}
    public record PerguntaResponse(Long id, String categoria, String enunciado, String codigo, List<AlternativaResponse> alternativas) {}
    public record InicioResponse(Long tentativaId, String dificuldade, List<PerguntaResponse> questoes) {}
    public record RespostaRequest(Long questaoId, Long alternativaId) {}
    public record DetalheResultado(Long questaoId, Long alternativaSelecionadaId, boolean correta,
                                   Long alternativaCorretaId, String textoAlternativaCorreta, String explicacao) {}
    public record ResultadoResponse(Long tentativaId, int totalQuestoes, int acertos, int pontuacao,
                                     int pontosPorAcerto, List<DetalheResultado> detalhes) {}
}
