package com.tiempo.clima.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ConsultaDTO {

    private Long usuarioId;
    private String nombreUsuario;
    private String tipoConsulta;
    private String ciudad;
    private String resultado;
    private LocalDateTime fechaConsulta;
}
