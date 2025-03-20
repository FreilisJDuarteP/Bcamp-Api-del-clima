package com.tiempo.clima.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    private String ciudad;

    @Column(columnDefinition = "LONGTEXT")
    private String resultado;

    private LocalDateTime fechaConsulta;

    @Column(nullable = false)
    private String tipoConsulta;
}
