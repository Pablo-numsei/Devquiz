package com.devquiz.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    boolean existsByApelidoIgnoreCase(String apelido);
    Optional<Usuario> findByApelidoIgnoreCase(String apelido);
}
