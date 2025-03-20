package com.tiempo.clima.service;

import com.tiempo.clima.dto.ConsultaDTO;
import com.tiempo.clima.entity.Consulta;
import com.tiempo.clima.entity.Usuario;
import com.tiempo.clima.repository.ConsultaRepository;
import com.tiempo.clima.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final UsuarioRepository usuarioRepository;

    // Guardar consulta con tipo
    public void registrarConsulta(Usuario usuario, String ciudad, String resultado, String tipoConsulta) {
        Consulta consulta = new Consulta();
        consulta.setUsuario(usuario);
        consulta.setCiudad(ciudad);
        consulta.setResultado(resultado);
        consulta.setTipoConsulta(tipoConsulta);
        consulta.setFechaConsulta(java.time.LocalDateTime.now());

        consultaRepository.save(consulta);
    }

    public List<ConsultaDTO> obtenerConsultasPorUsuario(String nombreUsuario) {
        Usuario usuario = usuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return consultaRepository.findByUsuario(usuario).stream()
                .map(consulta -> new ConsultaDTO(
                        usuario.getId(),
                        usuario.getNombreUsuario(),
                        consulta.getTipoConsulta(),
                        consulta.getCiudad(),
                        consulta.getResultado(),
                        consulta.getFechaConsulta()
                )).collect(Collectors.toList());
    }

    // Obtener todas las consultas (solo para ADMIN)
    public List<ConsultaDTO> obtenerTodasLasConsultas() {
        return consultaRepository.findAll().stream()
                .map(consulta -> new ConsultaDTO(
                        consulta.getUsuario().getId(),
                        consulta.getUsuario().getNombreUsuario(),
                        consulta.getTipoConsulta(),
                        consulta.getCiudad(),
                        consulta.getResultado(),
                        consulta.getFechaConsulta()
                )).collect(Collectors.toList());
    }
}
