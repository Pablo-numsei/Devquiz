package com.devquiz.quiz;

import jakarta.persistence.*;

@Entity
@Table(name = "alternativas")
public class Alternativa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "questao_id", nullable = false)
    private Questao questao;

    @Column(nullable = false, length = 500)
    private String texto;

    @Column(nullable = false)
    private Boolean correta;

    protected Alternativa() {}

    public Long getId() { return id; }
    public Questao getQuestao() { return questao; }
    public String getTexto() { return texto; }
    public Boolean getCorreta() { return correta; }
}
