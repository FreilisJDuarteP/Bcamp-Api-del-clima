package com.tiempo.clima.controller;

import com.tiempo.clima.dto.ContaminacionResponse;
import com.tiempo.clima.dto.Mensaje;
import com.tiempo.clima.entity.Usuario;
import com.tiempo.clima.service.ConsultaService;
import com.tiempo.clima.service.ContaminacionService;
import com.tiempo.clima.service.RateLimiterService;
import com.tiempo.clima.service.UsuarioService;
import io.github.bucket4j.Bucket;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;

@Tag(name = "3. Contaminación", description = "Endpoints para consultar la contaminación del aire")
@RestController
@RequestMapping("/contaminacion")
@CrossOrigin
public class ContaminacionController {

    private final ContaminacionService contaminacionService;
    private final UsuarioService usuarioService;
    private final ConsultaService consultaService;
    private final RateLimiterService rateLimiterService;

    public ContaminacionController(
            ContaminacionService contaminacionService,
            UsuarioService usuarioService,
            ConsultaService consultaService,
            RateLimiterService rateLimiterService) {
        this.contaminacionService = contaminacionService;
        this.usuarioService = usuarioService;
        this.consultaService = consultaService;
        this.rateLimiterService = rateLimiterService;
    }

    @Operation(
            summary = "Obtener calidad del aire por ciudad",
            description = "Consulta la calidad del aire en una ciudad específica. Se requiere autenticación."
    )
    @ApiResponse(responseCode = "200", description = "Consulta exitosa. Retorna la calidad del aire en la ciudad especificada.")
    @ApiResponse(responseCode = "400", description = "Error en la solicitud.")
    @ApiResponse(responseCode = "401", description = "No autorizado.")
    @ApiResponse(responseCode = "429", description = "Límite de consultas alcanzado.")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/ciudad/{nombreCiudad}")
    public ResponseEntity<?> obtenerCalidadAire(
            @Parameter(description = "Nombre de la ciudad para consultar la calidad del aire", required = true)
            @PathVariable String nombreCiudad,
            Authentication authentication) {

        String username = authentication.getName();

        Usuario usuario = usuarioService.getByNombreUsuario(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Bucket bucket = rateLimiterService.resolveBucket(username);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(429).body(new Mensaje("Límite de consultas alcanzado. Intenta de nuevo en una hora."));
        }

        try {
            ContaminacionResponse calidadAire = contaminacionService.obtenerCalidadAire(nombreCiudad, username);
            consultaService.registrarConsulta(usuario, nombreCiudad, calidadAire.toString(), "Consulta de contaminación");
            return ResponseEntity.ok(calidadAire);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new Mensaje("Error: " + e.getMessage()));
        }
    }
}
