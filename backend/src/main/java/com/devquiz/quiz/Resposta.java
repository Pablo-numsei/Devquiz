package com.devquiz.quiz;

import jakarta.persistence.*;

@Entity
@Table(name = "respostas")
public class Resposta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tentativa_id", nullable = false)
    private Tentativa tentativa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "questao_id", nullable = false)
    private Questao questao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alternativa_id")
    private Alternativa alternativa;

    @Column(nullable = false)
    private Boolean correta;

    protected Resposta() {}

    public Resposta(Tentativa tentativa, Questao questao, Alternativa alternativa, boolean correta) {
        this.tentativa = tentativa;
        this.questao = questao;
        this.alternativa = alternativa;
        this.correta = correta;
    }

    public void atualizar(Alternativa alternativa, boolean correta) {
        this.alternativa = alternativa;
        this.correta = correta;
    }

    public Long getId() { return id; }
    public Tentativa getTentativa() { return tentativa; }
    public Questao getQuestao() { return questao; }
    public Alternativa getAlternativa() { return alternativa; }
    public Boolean getCorreta() { return correta; }
}
