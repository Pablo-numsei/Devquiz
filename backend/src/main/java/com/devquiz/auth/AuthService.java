package com.devquiz.auth;

import com.devquiz.user.Usuario;
import com.devquiz.user.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthController.AuthResponse cadastrar(String apelidoInformado, String pin) {
        String apelido = normalizarApelido(apelidoInformado);
        validarPin(pin);
        if (usuarios.existsByApelidoIgnoreCase(apelido)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esse apelido já está em uso.");
        }
        Usuario usuario = usuarios.save(new Usuario(apelido, passwordEncoder.encode(pin)));
        return resposta(usuario);
    }

    public AuthController.AuthResponse login(String apelidoInformado, String pin) {
        String apelido = normalizarApelido(apelidoInformado);
        Usuario usuario = usuarios.findByApelidoIgnoreCase(apelido)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Apelido ou PIN incorreto."));
        if (pin == null || !passwordEncoder.matches(pin, usuario.getPinHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Apelido ou PIN incorreto.");
        }
        return resposta(usuario);
    }

    private AuthController.AuthResponse resposta(Usuario usuario) {
        return new AuthController.AuthResponse(
                jwtService.generateToken(usuario.getApelido()),
                new AuthController.UsuarioResumo(usuario.getId(), usuario.getApelido()));
    }

    private String normalizarApelido(String valor) {
        if (valor == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um apelido.");
        String apelido = valor.trim();
        if (!apelido.matches("[A-Za-z0-9_]{3,20}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use de 3 a 20 caracteres: letras, números ou _.");
        }
        return apelido;
    }

    private void validarPin(String pin) {
        if (pin == null || !pin.matches("\\d{4}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O PIN deve conter exatamente 4 dígitos.");
        }
    }
}
