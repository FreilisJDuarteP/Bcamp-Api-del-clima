package com.tiempo.clima.controller;

import com.tiempo.clima.dto.Mensaje;
import com.tiempo.clima.dto.PronosticoResponse;
import com.tiempo.clima.service.PronosticoService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PronosticoControllerTest {

    @Mock
    private PronosticoService pronosticoService;

    @Mock
    private RateLimiterService rateLimiterService;

    @Mock
    private Bucket bucket;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PronosticoController pronosticoController;

    @BeforeEach
    void setUp() {
        pronosticoController = new PronosticoController(pronosticoService, rateLimiterService);
    }

    @Test
    void solicitudEsExitosa() {

        String ciudad = "Bogotá";
        String username = "testUser";
        PronosticoResponse mockResponse = new PronosticoResponse(ciudad, null);

        when(authentication.getName()).thenReturn(username);
        when(rateLimiterService.resolveBucket(username)).thenReturn(bucket);
        when(bucket.tryConsume(1)).thenReturn(true);
        when(pronosticoService.obtenerPronostico(ciudad, username)).thenReturn(mockResponse);


        ResponseEntity<?> response = pronosticoController.obtenerPronostico(ciudad, authentication);


        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(mockResponse, response.getBody());

        verify(pronosticoService).obtenerPronostico(ciudad, username);
    }

    @Test
    void limiteDeConsultasAlcanzado() {

        String ciudad = "Bogotá";
        String username = "testUser";

        when(authentication.getName()).thenReturn(username);
        when(rateLimiterService.resolveBucket(username)).thenReturn(bucket);
        when(bucket.tryConsume(1)).thenReturn(false);


        ResponseEntity<?> response = pronosticoController.obtenerPronostico(ciudad, authentication);


        assertNotNull(response);
        assertEquals(429, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof Mensaje);
        assertEquals("Límite de consultas alcanzado. Intenta de nuevo en una hora.", ((Mensaje) response.getBody()).getMensaje());

        verify(pronosticoService, never()).obtenerPronostico(anyString(), anyString());
    }

    @Test
    void errorCuandoServicioFalla() {

        String ciudad = "Bogotá";
        String username = "testUser";

        when(authentication.getName()).thenReturn(username);
        when(rateLimiterService.resolveBucket(username)).thenReturn(bucket);
        when(bucket.tryConsume(1)).thenReturn(true);
        when(pronosticoService.obtenerPronostico(ciudad, username)).thenThrow(new RuntimeException("Error interno"));


        ResponseEntity<?> response = pronosticoController.obtenerPronostico(ciudad, authentication);

        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof Mensaje);

        verify(pronosticoService).obtenerPronostico(ciudad, username);
    }
}
