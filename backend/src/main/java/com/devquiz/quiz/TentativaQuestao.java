package com.devquiz.quiz;

import jakarta.persistence.*;

@Entity
@Table(name = "tentativa_questoes")
public class TentativaQuestao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tentativa_id", nullable = false)
    private Tentativa tentativa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "questao_id", nullable = false)
    private Questao questao;

    protected TentativaQuestao() {}

    public TentativaQuestao(Tentativa tentativa, Questao questao) {
        this.tentativa = tentativa;
        this.questao = questao;
    }

    public Long getId() { return id; }
    public Tentativa getTentativa() { return tentativa; }
    public Questao getQuestao() { return questao; }
}
