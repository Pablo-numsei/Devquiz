package com.devquiz.quiz;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TentativaRepository extends JpaRepository<Tentativa, Long> {
    Optional<Tentativa> findByIdAndUsuario_Id(Long id, Long usuarioId);
    List<Tentativa> findByUsuario_IdAndStatusOrderByCriadoEmDesc(Long usuarioId, String status);
}
