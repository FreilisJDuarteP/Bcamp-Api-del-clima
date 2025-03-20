package com.tiempo.clima.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class NuevoUsuario {
    private String nombreUsuario;
    private String email;
    private String password;
    private Set<String> roles;
}
