package com.devquiz.user;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "apelido", nullable = false, unique = true, length = 20)
    private String apelido;

    @Column(name = "pin_hash", nullable = false, length = 100)
    private String pinHash;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    protected Usuario() {}

    public Usuario(String apelido, String pinHash) {
        this.apelido = apelido;
        this.pinHash = pinHash;
        this.criadoEm = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getApelido() { return apelido; }
    public String getPinHash() { return pinHash; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
