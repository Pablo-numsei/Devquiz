package com.devquiz.quiz;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {
    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/categorias")
    public List<QuizService.CategoriaResponse> categorias() {
        return quizService.listarCategorias();
    }

    @PostMapping("/tentativas")
    @ResponseStatus(HttpStatus.CREATED)
    public QuizService.InicioResponse iniciar(
            Authentication authentication,
            @Valid @RequestBody InicioRequest request) {
        return quizService.iniciar(
                authentication.getName(),
                request.categoriaIds(),
                request.dificuldade(),
                request.quantidade());
    }

    @GetMapping("/tentativas/pendentes")
    public List<QuizService.PendenteResponse> pendentes(Authentication authentication) {
        return quizService.listarPendentes(authentication.getName());
    }

    @GetMapping("/tentativas/historico")
    public List<QuizService.HistoricoResponse> historico(Authentication authentication) {
        return quizService.listarHistorico(authentication.getName());
    }

    @GetMapping("/tentativas/{id}/revisao")
    public QuizService.RevisaoResponse revisar(
            Authentication authentication,
            @PathVariable Long id) {
        return quizService.revisarTentativa(authentication.getName(), id);
    }

    @GetMapping("/tentativas/{id}")
    public QuizService.InicioResponse obter(
            Authentication authentication,
            @PathVariable Long id) {
        return quizService.obterTentativa(authentication.getName(), id);
    }

    @PutMapping("/tentativas/{id}/respostas")
    public QuizService.FeedbackResponse salvarResposta(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody RespostaRequest request) {
        return quizService.salvarResposta(
                authentication.getName(),
                id,
                new QuizService.RespostaRequest(request.questaoId(), request.alternativaId()));
    }

    @PostMapping("/tentativas/{id}/finalizar")
    public QuizService.ResultadoResponse finalizar(
            Authentication authentication,
            @PathVariable Long id) {
        return quizService.finalizar(authentication.getName(), id);
    }

    public record InicioRequest(
            @NotEmpty @Size(max = 6) List<@NotNull Integer> categoriaIds,
            @NotNull String dificuldade,
            Integer quantidade) {}

    public record RespostaRequest(
            @NotNull Long questaoId,
            Long alternativaId) {}
}
