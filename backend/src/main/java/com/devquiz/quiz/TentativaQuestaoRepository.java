package com.devquiz.quiz;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TentativaQuestaoRepository extends JpaRepository<TentativaQuestao, Long> {
    List<TentativaQuestao> findByTentativa_Id(Long tentativaId);
}
