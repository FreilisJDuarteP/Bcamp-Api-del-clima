package com.tiempo.clima.controller;

import com.tiempo.clima.dto.ConsultaDTO;
import com.tiempo.clima.service.ConsultaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "6. Consultas", description = "Endpoints para consultar el historial de busqueda")
@RestController
@RequestMapping("/consultas")
@CrossOrigin
public class ConsultaController {

    private final ConsultaService consultaService;

    public ConsultaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    @Operation(summary = "Obtener historial de consultas del usuario autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial de consultas obtenido correctamente"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado"),
            @ApiResponse(responseCode = "500", description = "Error inesperado del servidor")
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/miconsulta")
    public ResponseEntity<List<ConsultaDTO>> obtenerMisConsultas(
            @AuthenticationPrincipal UserDetails userDetails) {
        String nombreUsuario = userDetails.getUsername();
        return ResponseEntity.ok(consultaService.obtenerConsultasPorUsuario(nombreUsuario));
    }

    @Operation(summary = "Obtener historial de todas las consultas (solo para ADMIN)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial de consultas obtenido correctamente"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado"),
            @ApiResponse(responseCode = "500", description = "Error inesperado del servidor")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/todas")
    public ResponseEntity<List<ConsultaDTO>> obtenerTodasLasConsultas() {
        return ResponseEntity.ok(consultaService.obtenerTodasLasConsultas());
    }
}
