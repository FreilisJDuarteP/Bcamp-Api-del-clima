package com.tiempo.clima.controller;

import com.tiempo.clima.dto.ClimaResponse;
import com.tiempo.clima.dto.Mensaje;
import com.tiempo.clima.service.ClimaService;
import com.tiempo.clima.service.RateLimiterService;
import io.github.bucket4j.Bucket;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "2. Clima", description = "Endpoints para consultar el clima actual por medio de ciudades")
@RestController
@RequestMapping("/clima")
@CrossOrigin
public class ClimaController {

    private final ClimaService climaService;
    private final RateLimiterService rateLimiterService;

    public ClimaController(ClimaService climaService, RateLimiterService rateLimiterService) {
        this.climaService = climaService;
        this.rateLimiterService = rateLimiterService;
    }

    @Operation(
            summary = "Obtener clima de una ciudad",
            description = "Devuelve el clima actual para una ciudad específica.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Clima obtenido correctamente"),
                    @ApiResponse(responseCode = "400", description = "Solicitud incorrecta"),
                    @ApiResponse(responseCode = "429", description = "Límite de consultas alcanzado"),
                    @ApiResponse(responseCode = "401", description = "No autorizado")
            }
    )
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/ciudad/{nombreCiudad}")
    public ResponseEntity<?> obtenerClima(
            @Parameter(description = "Nombre de la ciudad", example = "Bogotá")
            @PathVariable String nombreCiudad,
            Authentication authentication) {

        String username = authentication.getName();
        Bucket bucket = rateLimiterService.resolveBucket(username);

        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(429).body(new Mensaje("Límite de consultas alcanzado. Intenta de nuevo en una hora."));
        }

        try {

            ClimaResponse clima = climaService.obtenerClima(nombreCiudad, username);
            return ResponseEntity.ok(clima);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new Mensaje("Error al obtener el clima: " + e.getMessage()));
        }
    }
}
