package com.tiempo.clima.dto;

import lombok.Data;

@Data
public class ContaminacionResponse {
    private String ciudad;
    private String calidadAire;

    public ContaminacionResponse(String ciudad, String calidadAire) {
        this.ciudad = ciudad;
        this.calidadAire = calidadAire;
    }
}
