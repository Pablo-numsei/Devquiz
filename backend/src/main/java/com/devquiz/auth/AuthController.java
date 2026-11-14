package com.devquiz.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse cadastrar(@Valid @RequestBody AuthRequest request) {
        return authService.cadastrar(request.apelido(), request.pin());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        return authService.login(request.apelido(), request.pin());
    }

    public record AuthRequest(
            @NotBlank @Size(min = 3, max = 20)
            @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "Use apenas letras, números e _ no apelido.")
            String apelido,
            @NotBlank @Pattern(regexp = "^\\d{4}$", message = "O PIN deve ter 4 dígitos.")
            String pin) {}

    public record UsuarioResumo(Long id, String apelido) {}
    public record AuthResponse(String token, UsuarioResumo usuario) {}
}
