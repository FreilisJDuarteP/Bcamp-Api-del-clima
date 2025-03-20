package com.tiempo.clima.dto;
import lombok.Data;
@Data
public class ClimaResponse {

    private String nombre;
    private double temperatura;
    private double tempMin;
    private double tempMax;
    private double sensacionTermica;
    private String descripcion;


    public ClimaResponse(String nombre, double temperatura, double tempMin, double tempMax, double sensacionTermica, String descripcion) {
        this.nombre = nombre;
        this.temperatura = temperatura;
        this.tempMin = tempMin;
        this.tempMax = tempMax;
        this.sensacionTermica = sensacionTermica;
        this.descripcion = descripcion;
    }






}
