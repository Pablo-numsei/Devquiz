package com.devquiz.quiz;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TentativaRepository extends JpaRepository<Tentativa, Long> {
    Optional<Tentativa> findByIdAndUsuarioId(Long id, Long usuarioId);
}
