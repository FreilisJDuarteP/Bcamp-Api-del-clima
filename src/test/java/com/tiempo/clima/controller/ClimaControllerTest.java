package com.tiempo.clima.controller;

import com.tiempo.clima.dto.ClimaResponse;
import com.tiempo.clima.dto.Mensaje;
import com.tiempo.clima.service.ClimaService;
import com.tiempo.clima.service.RateLimiterService;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClimaControllerTest {

    @Mock
    private ClimaService climaService;

    @Mock
    private RateLimiterService rateLimiterService;

    @Mock
    private Authentication authentication;

    @Mock
    private Bucket bucket;

    @InjectMocks
    private ClimaController climaController;

    @BeforeEach
    void setUp() {
        climaController = new ClimaController(climaService, rateLimiterService);
    }

    @Test
    void LaSolicitudEsExitosa() {

        String ciudad = "Bogotá";
        String username = "testUser";

        ClimaResponse mockResponse = new ClimaResponse(
                ciudad, 20.5, 18.0, 25.0, 21.0, "Cielo despejado"
        );

        when(authentication.getName()).thenReturn(username);
        when(rateLimiterService.resolveBucket(username)).thenReturn(bucket);
        when(bucket.tryConsume(1)).thenReturn(true);
        when(climaService.obtenerClima(ciudad, username)).thenReturn(mockResponse);


        ResponseEntity<?> response = climaController.obtenerClima(ciudad, authentication);


        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        ClimaResponse responseBody = (ClimaResponse) response.getBody();
        assertNotNull(responseBody);
        assertEquals(ciudad, responseBody.getNombre());
        assertEquals(20.5, responseBody.getTemperatura());

        verify(climaService).obtenerClima(ciudad, username);
    }

    @Test
    void LimiteDeConsultasAlcanzado() {

        String ciudad = "Bogotá";
        String username = "testUser";

        when(authentication.getName()).thenReturn(username);
        when(rateLimiterService.resolveBucket(username)).thenReturn(bucket);
        when(bucket.tryConsume(1)).thenReturn(false);

        ResponseEntity<?> response = climaController.obtenerClima(ciudad, authentication);

        assertNotNull(response);
        assertEquals(429, response.getStatusCode().value());

        Mensaje mensaje = (Mensaje) response.getBody();
        assertNotNull(mensaje);
        assertEquals("Límite de consultas alcanzado. Intenta de nuevo en una hora.", mensaje.getMensaje());

        verify(climaService, never()).obtenerClima(anyString(), anyString());
    }

    @Test
    void ErrorCuandoServicioFalla() {

        String ciudad = "Bogotá";
        String username = "testUser";

        when(authentication.getName()).thenReturn(username);
        when(rateLimiterService.resolveBucket(username)).thenReturn(bucket);
        when(bucket.tryConsume(1)).thenReturn(true);
        when(climaService.obtenerClima(ciudad, username))
                .thenThrow(new RuntimeException("Error interno"));

        ResponseEntity<?> response = climaController.obtenerClima(ciudad, authentication);

        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());

        Mensaje mensaje = (Mensaje) response.getBody();
        assertNotNull(mensaje);
        assertTrue(mensaje.getMensaje().contains("Error al obtener el clima: Error interno"));

        verify(climaService).obtenerClima(ciudad, username);
    }
}
