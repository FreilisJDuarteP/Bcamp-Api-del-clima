package com.tiempo.clima.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PronosticoDTO {
    private String ciudad;
    private double temperatura;
    private String descripcion;
    private int humedad;
    private double velocidadViento;
    private double presion;
    private String iconoClima;
    private long fechaHora;
}
