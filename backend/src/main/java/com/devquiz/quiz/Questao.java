package com.devquiz.quiz;

import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questoes")
public class Questao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(nullable = false, length = 20)
    private String dificuldade;

    @Nationalized
    @Column(nullable = false, length = 1000)
    private String enunciado;

    @Nationalized
    @Column(columnDefinition = "nvarchar(max)")
    private String codigo;

    @Nationalized
    @Column(nullable = false, columnDefinition = "nvarchar(max)")
    private String explicacao;

    @Column(nullable = false)
    private Boolean ativa;

    @OneToMany(mappedBy = "questao", fetch = FetchType.LAZY)
    private List<Alternativa> alternativas = new ArrayList<>();

    protected Questao() {}

    public Long getId() { return id; }
    public Categoria getCategoria() { return categoria; }
    public String getDificuldade() { return dificuldade; }
    public String getEnunciado() { return enunciado; }
    public String getCodigo() { return codigo; }
    public String getExplicacao() { return explicacao; }
    public Boolean getAtiva() { return ativa; }
    public List<Alternativa> getAlternativas() { return alternativas; }
}
