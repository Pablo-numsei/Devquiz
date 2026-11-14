package com.devquiz.quiz;

import jakarta.persistence.*;

@Entity
@Table(name = "categorias")
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 60)
    private String nome;

    @Column(nullable = false, length = 160)
    private String descricao;

    @Column(nullable = false)
    private Boolean ativa;

    protected Categoria() {}

    public Integer getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public Boolean getAtiva() { return ativa; }
}
