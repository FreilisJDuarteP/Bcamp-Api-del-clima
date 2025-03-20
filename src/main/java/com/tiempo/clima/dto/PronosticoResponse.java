package com.tiempo.clima.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PronosticoResponse {
    private String ciudad;
    private List<PronosticoDTO> pronostico;
}
