package com.devquiz.quiz;

import com.devquiz.user.Usuario;
import com.devquiz.user.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
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

        Usuario usuario = buscarUsuario(apelido);
        String nivel = normalizarDificuldade(dificuldade);
        int quantidade = quantidadeInformada == null ? QUANTIDADE_PADRAO : quantidadeInformada;
        if (quantidade < 1 || quantidade > QUANTIDADE_PADRAO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A quantidade deve estar entre 1 e 10.");
        }

        List<Integer> idsUnicos = categoriaIds.stream().filter(Objects::nonNull).distinct().toList();
        long categoriasAtivas = categorias.findAllById(idsUnicos).stream()
                .filter(c -> Boolean.TRUE.equals(c.getAtiva())).count();
        if (idsUnicos.isEmpty() || categoriasAtivas != idsUnicos.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uma ou mais categorias são inválidas.");
        }

        // Prioriza perguntas que o usuário ainda não recebeu em tentativas anteriores.
        List<Questao> candidatas = new ArrayList<>(
                questoes.buscarNaoUtilizadas(idsUnicos, nivel, usuario.getId()));

        // Quando o conjunto de inéditas é pequeno, completa com questões já utilizadas.
        if (candidatas.size() < quantidade) {
            Set<Long> jaIncluidas = candidatas.stream().map(Questao::getId).collect(Collectors.toSet());
            List<Questao> restantes = questoes.buscarDisponiveis(idsUnicos, nivel).stream()
                    .filter(q -> !jaIncluidas.contains(q.getId()))
                    .toList();
            candidatas.addAll(restantes);
        }

        List<Questao> disponiveis = candidatas.stream()
                .filter(this::questaoValida)
                .collect(Collectors.toCollection(ArrayList::new));

        if (disponiveis.size() < quantidade) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ainda não existem questões suficientes e válidas para essa seleção. Disponíveis: "
                            + disponiveis.size() + ". Selecione mais categorias ou aguarde novas questões.");
        }

        Collections.shuffle(disponiveis);
        List<Questao> selecionadas = new ArrayList<>(disponiveis.subList(0, quantidade));

        Tentativa tentativa = tentativas.save(new Tentativa(usuario, nivel, quantidade));
        tentativaQuestoes.saveAll(selecionadas.stream()
                .map(q -> new TentativaQuestao(tentativa, q))
                .toList());

        return montarInicio(tentativa, selecionadas, Map.of());
    }

    @Transactional(readOnly = true)
    public List<PendenteResponse> listarPendentes(String apelido) {
        Usuario usuario = buscarUsuario(apelido);
        return tentativas.findByUsuario_IdAndStatusOrderByCriadoEmDesc(usuario.getId(), "EM_ANDAMENTO")
                .stream()
                .map(tentativa -> {
                    List<TentativaQuestao> itens = tentativaQuestoes.findByTentativa_Id(tentativa.getId());
                    int respondidas = respostas.findByTentativa_Id(tentativa.getId()).size();
                    List<String> nomesCategorias = itens.stream()
                            .map(item -> item.getQuestao().getCategoria().getNome())
                            .distinct()
                            .toList();
                    return new PendenteResponse(
                            tentativa.getId(),
                            tentativa.getDificuldade(),
                            tentativa.getTotalQuestoes(),
                            respondidas,
                            nomesCategorias,
                            tentativa.getCriadoEm());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HistoricoResponse> listarHistorico(String apelido) {
        Usuario usuario = buscarUsuario(apelido);
        return tentativas.findByUsuario_IdAndStatusOrderByCriadoEmDesc(usuario.getId(), "FINALIZADA")
                .stream()
                .map(tentativa -> {
                    List<String> nomesCategorias = tentativaQuestoes.findByTentativa_Id(tentativa.getId()).stream()
                            .map(item -> item.getQuestao().getCategoria().getNome())
                            .distinct()
                            .toList();
                    return new HistoricoResponse(
                            tentativa.getId(),
                            nomesCategorias,
                            tentativa.getDificuldade(),
                            tentativa.getTotalQuestoes(),
                            tentativa.getAcertos(),
                            tentativa.getPontuacao(),
                            tentativa.getCriadoEm());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public InicioResponse obterTentativa(String apelido, Long tentativaId) {
        Usuario usuario = buscarUsuario(apelido);
        Tentativa tentativa = buscarTentativaDoUsuario(tentativaId, usuario.getId());
        if (!"EM_ANDAMENTO".equals(tentativa.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta tentativa já foi finalizada.");
        }

        Map<Long, Long> selecionadas = respostas.findByTentativa_Id(tentativaId).stream()
                .filter(r -> r.getAlternativa() != null)
                .collect(Collectors.toMap(
                        r -> r.getQuestao().getId(),
                        r -> r.getAlternativa().getId()));

        List<Questao> perguntas = tentativaQuestoes.findByTentativa_Id(tentativaId).stream()
                .map(TentativaQuestao::getQuestao)
                .toList();
        return montarInicio(tentativa, perguntas, selecionadas);
    }

    @Transactional
    public FeedbackResponse salvarResposta(String apelido, Long tentativaId, RespostaRequest request) {
        if (request == null || request.questaoId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a questão respondida.");
        }

        Usuario usuario = buscarUsuario(apelido);
        Tentativa tentativa = buscarTentativaDoUsuario(tentativaId, usuario.getId());
        validarTentativaEmAndamento(tentativa);

        TentativaQuestao item = tentativaQuestoes.findByTentativa_Id(tentativaId).stream()
                .filter(i -> i.getQuestao().getId().equals(request.questaoId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Essa questão não pertence à tentativa."));

        Questao questao = item.getQuestao();
        Alternativa alternativa = null;
        if (request.alternativaId() != null) {
            alternativa = questao.getAlternativas().stream()
                    .filter(a -> a.getId().equals(request.alternativaId()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "A alternativa não pertence à questão informada."));
        }

        boolean correta = alternativa != null && Boolean.TRUE.equals(alternativa.getCorreta());
        Optional<Resposta> existente = respostas.findByTentativa_IdAndQuestao_Id(tentativaId, questao.getId());
        if (existente.isPresent()) {
            existente.get().atualizar(alternativa, correta);
            respostas.save(existente.get());
        } else {
            respostas.save(new Resposta(tentativa, questao, alternativa, correta));
        }

        Alternativa corretaDaQuestao = questao.getAlternativas().stream()
                .filter(a -> Boolean.TRUE.equals(a.getCorreta()))
                .findFirst()
                .orElse(null);

        return new FeedbackResponse(
                questao.getId(),
                correta,
                alternativa == null ? null : alternativa.getId(),
                corretaDaQuestao == null ? null : corretaDaQuestao.getId(),
                corretaDaQuestao == null ? null : corretaDaQuestao.getTexto(),
                questao.getExplicacao());
    }

    @Transactional
    public ResultadoResponse finalizar(String apelido, Long tentativaId) {
        Usuario usuario = buscarUsuario(apelido);
        Tentativa tentativa = buscarTentativaDoUsuario(tentativaId, usuario.getId());
        validarTentativaEmAndamento(tentativa);

        List<TentativaQuestao> itens = tentativaQuestoes.findByTentativa_Id(tentativaId);
        if (itens.size() != tentativa.getTotalQuestoes()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A tentativa está inconsistente. Inicie outra.");
        }

        Map<Long, Resposta> respostasSalvas = respostas.findByTentativa_Id(tentativaId).stream()
                .collect(Collectors.toMap(r -> r.getQuestao().getId(), r -> r));

        int acertos = 0;
        int pontosPorAcerto = pontosPorDificuldade(tentativa.getDificuldade());
        List<DetalheResultado> detalhes = new ArrayList<>();

        for (TentativaQuestao item : itens) {
            Questao questao = item.getQuestao();
            Resposta resposta = respostasSalvas.get(questao.getId());
            Alternativa selecionada = resposta == null ? null : resposta.getAlternativa();
            boolean correta = resposta != null && Boolean.TRUE.equals(resposta.getCorreta());
            if (correta) acertos++;

            Alternativa corretaDaQuestao = questao.getAlternativas().stream()
                    .filter(a -> Boolean.TRUE.equals(a.getCorreta()))
                    .findFirst()
                    .orElse(null);

            detalhes.add(new DetalheResultado(
                    questao.getId(),
                    selecionada == null ? null : selecionada.getId(),
                    correta,
                    corretaDaQuestao == null ? null : corretaDaQuestao.getId(),
                    corretaDaQuestao == null ? null : corretaDaQuestao.getTexto(),
                    questao.getExplicacao()));
        }

        int pontuacao = acertos * pontosPorAcerto;
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

    private InicioResponse montarInicio(Tentativa tentativa, List<Questao> perguntas, Map<Long, Long> selecionadas) {
        List<PerguntaResponse> respostas = perguntas.stream()
                .map(q -> new PerguntaResponse(
                        q.getId(),
                        q.getCategoria().getNome(),
                        q.getEnunciado(),
                        q.getCodigo(),
                        q.getAlternativas().stream()
                                .map(a -> new AlternativaResponse(a.getId(), a.getTexto()))
                                .toList(),
                        selecionadas.get(q.getId())))
                .toList();
        return new InicioResponse(tentativa.getId(), tentativa.getDificuldade(), respostas);
    }

    private boolean questaoValida(Questao questao) {
        return questao.getAlternativas() != null
                && questao.getAlternativas().size() == 4
                && questao.getAlternativas().stream().filter(a -> Boolean.TRUE.equals(a.getCorreta())).count() == 1;
    }

    private Usuario buscarUsuario(String apelido) {
        return usuarios.findByApelidoIgnoreCase(apelido)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado."));
    }

    private Tentativa buscarTentativaDoUsuario(Long tentativaId, Long usuarioId) {
        return tentativas.findByIdAndUsuario_Id(tentativaId, usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tentativa não encontrada."));
    }

    private void validarTentativaEmAndamento(Tentativa tentativa) {
        if (!"EM_ANDAMENTO".equals(tentativa.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta tentativa já foi finalizada.");
        }
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
    public record PerguntaResponse(Long id, String categoria, String enunciado, String codigo,
                                   List<AlternativaResponse> alternativas, Long alternativaSelecionadaId) {}
    public record InicioResponse(Long tentativaId, String dificuldade, List<PerguntaResponse> questoes) {}
    public record RespostaRequest(Long questaoId, Long alternativaId) {}
    public record FeedbackResponse(Long questaoId, boolean correta, Long alternativaSelecionadaId,
                                   Long alternativaCorretaId, String textoAlternativaCorreta, String explicacao) {}
    public record DetalheResultado(Long questaoId, Long alternativaSelecionadaId, boolean correta,
                                   Long alternativaCorretaId, String textoAlternativaCorreta, String explicacao) {}
    public record ResultadoResponse(Long tentativaId, int totalQuestoes, int acertos, int pontuacao,
                                     int pontosPorAcerto, List<DetalheResultado> detalhes) {}
    public record PendenteResponse(Long tentativaId, String dificuldade, int totalQuestoes,
                                   int respondidas, List<String> categorias, LocalDateTime criadoEm) {}
    public record HistoricoResponse(Long tentativaId, List<String> categorias, String dificuldade,
                                    int totalQuestoes, int acertos, int pontuacao, LocalDateTime criadoEm) {}
}
