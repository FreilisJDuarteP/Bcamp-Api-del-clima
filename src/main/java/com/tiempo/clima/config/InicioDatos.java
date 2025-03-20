package com.tiempo.clima.config;

import com.tiempo.clima.entity.Rol;
import com.tiempo.clima.repository.RolRepository;
import com.tiempo.clima.security.enums.RolNombre;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class InicioDatos {

    private final RolRepository rolRepository;

    // Constructor de referencia inyectar rolRepository sin que rebote con los ya definidos.
    public InicioDatos(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }
//al iniciar spring comprueba si existen o no los dos reoles principales y los crea.
    @PostConstruct
    public void init() {
        if (rolRepository.findByNombre(RolNombre.USER).isEmpty()) {
            Rol rolUser = new Rol();
            rolUser.setNombre(RolNombre.USER);
            rolRepository.save(rolUser);
            System.out.println("Rol USER creado correctamente");
        }

        if (rolRepository.findByNombre(RolNombre.ADMIN).isEmpty()) {
            Rol rolAdmin = new Rol();
            rolAdmin.setNombre(RolNombre.ADMIN);
            rolRepository.save(rolAdmin);
            System.out.println("rol ADMIN creado correctamente");
        }
    }
}
