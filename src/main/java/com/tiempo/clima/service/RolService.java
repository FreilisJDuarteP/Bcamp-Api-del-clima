package com.tiempo.clima.service;

import com.tiempo.clima.entity.Rol;
import com.tiempo.clima.repository.RolRepository;
import com.tiempo.clima.security.enums.RolNombre;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RolService {

    private final RolRepository rolRepository;

    public RolService(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    public Optional<Rol> getByRolNombre(RolNombre rolNombre) {
        return rolRepository.findByNombre(rolNombre);
    }
}
