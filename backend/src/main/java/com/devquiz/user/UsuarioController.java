package com.devquiz.user;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioRepository usuarios;

    public UsuarioController(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @GetMapping("/perfil")
    public PerfilResponse perfil(Authentication authentication) {
        Usuario usuario = usuarios.findByApelidoIgnoreCase(authentication.getName())
                .orElseThrow();
        return new PerfilResponse(usuario.getId(), usuario.getApelido(), usuario.getCriadoEm());
    }

    public record PerfilResponse(Long id, String apelido, LocalDateTime criadoEm) {}
}
