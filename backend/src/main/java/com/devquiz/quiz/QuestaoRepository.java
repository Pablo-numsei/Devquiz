package com.devquiz.quiz;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface QuestaoRepository extends JpaRepository<Questao, Long> {
    @EntityGraph(attributePaths = {"categoria", "alternativas"})
    @Query("""
        SELECT DISTINCT q FROM Questao q
        WHERE q.categoria.id IN :categoriaIds
          AND q.dificuldade = :dificuldade
          AND q.ativa = true
        """)
    List<Questao> buscarDisponiveis(
            @Param("categoriaIds") List<Integer> categoriaIds,
            @Param("dificuldade") String dificuldade);

    @EntityGraph(attributePaths = {"categoria", "alternativas"})
    @Query("""
        SELECT DISTINCT q FROM Questao q
        WHERE q.categoria.id IN :categoriaIds
          AND q.dificuldade = :dificuldade
          AND q.ativa = true
          AND q.id NOT IN (
              SELECT tq.questao.id FROM TentativaQuestao tq
              WHERE tq.tentativa.usuario.id = :usuarioId
          )
        """)
    List<Questao> buscarNaoUtilizadas(
            @Param("categoriaIds") List<Integer> categoriaIds,
            @Param("dificuldade") String dificuldade,
            @Param("usuarioId") Long usuarioId);
}
