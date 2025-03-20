package com.tiempo.clima.controller;

import com.tiempo.clima.dto.Mensaje;
import com.tiempo.clima.dto.PronosticoResponse;
import com.tiempo.clima.service.PronosticoService;
import com.tiempo.clima.service.RateLimiterService;
import io.github.bucket4j.Bucket;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "4. Pronóstico", description = "Endpoints para consultar el pronóstico del tiempo para 5 días")
@RestController
@RequestMapping("/api/pronostico")
@CrossOrigin
public class PronosticoController {

    private final PronosticoService pronosticoService;
    private final RateLimiterService rateLimiterService;

    public PronosticoController(PronosticoService pronosticoService, RateLimiterService rateLimiterService) {
        this.pronosticoService = pronosticoService;
        this.rateLimiterService = rateLimiterService;
    }

    @Operation(summary = "Obtener pronóstico del clima por ciudad")
    @ApiResponse(responseCode = "200", description = "Pronóstico obtenido correctamente")
    @ApiResponse(responseCode = "400", description = "Error al obtener el pronóstico")
    @ApiResponse(responseCode = "429", description = "Límite de consultas alcanzado")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{ciudad}")
    public ResponseEntity<?> obtenerPronostico(
            @PathVariable String ciudad,
            Authentication authentication) {

        String username = authentication.getName();


        Bucket bucket = rateLimiterService.resolveBucket(username);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(429).body(new Mensaje("Límite de consultas alcanzado. Intenta de nuevo en una hora."));
        }

        try {
            PronosticoResponse pronostico = pronosticoService.obtenerPronostico(ciudad, username);
            return ResponseEntity.ok(pronostico);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new Mensaje("Error al obtener el pronóstico: " + e.getMessage()));
        }
    }
}
