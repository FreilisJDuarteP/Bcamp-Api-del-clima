package com.tiempo.clima.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cache.CacheManager;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "5. Caché", description = "Endpoints para borrar el cache")
@RestController
@RequestMapping("/cache")
@CrossOrigin
public class CacheController {

    private final CacheManager cacheManager;

    public CacheController(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Operation(
            summary = "Limpiar la caché",
            description = "Elimina todos los datos almacenados en la caché."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Caché limpiada correctamente"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado"),
            @ApiResponse(responseCode = "500", description = "Error inesperado del servidor")
    })
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/limpiar")
    public String limpiarCache() {
        cacheManager.getCacheNames().forEach(cache -> cacheManager.getCache(cache).clear());
        return "Caché limpiada";
    }
}
