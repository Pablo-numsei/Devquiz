package com.devquiz.quiz;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestaoRepository extends JpaRepository<Questao, Long> {
    List<Questao> findByCategoria_IdInAndDificuldadeAndAtivaTrue(
            List<Integer> categoriaIds, String dificuldade);
}
