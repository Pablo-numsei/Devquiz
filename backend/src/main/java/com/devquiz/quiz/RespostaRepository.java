package com.devquiz.quiz;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RespostaRepository extends JpaRepository<Resposta, Long> {
    List<Resposta> findByTentativa_Id(Long tentativaId);
    Optional<Resposta> findByTentativa_IdAndQuestao_Id(Long tentativaId, Long questaoId);
}
