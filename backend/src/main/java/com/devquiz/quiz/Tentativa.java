package com.devquiz.quiz;

import com.devquiz.user.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tentativas")
public class Tentativa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 20)
    private String dificuldade;

    @Column(name = "total_questoes", nullable = false)
    private Short totalQuestoes;

    @Column(nullable = false)
    private Short acertos;

    @Column(nullable = false)
    private Integer pontuacao;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(nullable = false, length = 20)
    private String status;

    protected Tentativa() {}

    public Tentativa(Usuario usuario, String dificuldade, int totalQuestoes) {
        this.usuario = usuario;
        this.dificuldade = dificuldade;
        this.totalQuestoes = (short) totalQuestoes;
        this.acertos = (short) 0;
        this.pontuacao = 0;
        this.criadoEm = LocalDateTime.now();
        this.status = "EM_ANDAMENTO";
    }

    public void finalizar(int acertos, int pontuacao) {
        if (!"EM_ANDAMENTO".equals(status)) {
            throw new IllegalStateException("Esta tentativa já foi finalizada.");
        }
        this.acertos = (short) acertos;
        this.pontuacao = pontuacao;
        this.status = "FINALIZADA";
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public String getDificuldade() { return dificuldade; }
    public Short getTotalQuestoes() { return totalQuestoes; }
    public Short getAcertos() { return acertos; }
    public Integer getPontuacao() { return pontuacao; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public String getStatus() { return status; }
}
