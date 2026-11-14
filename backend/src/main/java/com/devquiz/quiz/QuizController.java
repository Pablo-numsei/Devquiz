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

    @PostMapping("/tentativas/{id}/finalizar")
    public QuizService.ResultadoResponse finalizar(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody FinalizarRequest request) {
        return quizService.finalizar(
                authentication.getName(),
                id,
                request == null ? List.of() : request.respostas());
    }

    public record InicioRequest(
            @NotEmpty @Size(max = 6) List<@NotNull Integer> categoriaIds,
            @NotNull String dificuldade,
            Integer quantidade) {}

    public record FinalizarRequest(List<QuizService.RespostaRequest> respostas) {}
}
