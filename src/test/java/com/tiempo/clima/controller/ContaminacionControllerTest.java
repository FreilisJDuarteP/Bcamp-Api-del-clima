package com.tiempo.clima.controller;

import com.tiempo.clima.dto.ContaminacionResponse;
import com.tiempo.clima.dto.Mensaje;
import com.tiempo.clima.entity.Usuario;
import com.tiempo.clima.service.ConsultaService;
import com.tiempo.clima.service.ContaminacionService;
import com.tiempo.clima.service.RateLimiterService;
import com.tiempo.clima.service.UsuarioService;
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
class ContaminacionControllerTest {

    @Mock
    private ContaminacionService contaminacionService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private ConsultaService consultaService;

    @Mock
    private RateLimiterService rateLimiterService;

    @Mock
    private Bucket bucket;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ContaminacionController contaminacionController;

    @BeforeEach
    void setUp() {
        contaminacionController = new ContaminacionController(contaminacionService, usuarioService, consultaService, rateLimiterService);
    }

    @Test
    void solicitudEsExitosa() {

        String ciudad = "Bogotá";
        String username = "testUser";
        ContaminacionResponse mockResponse = new ContaminacionResponse(ciudad, "Bueno");

        when(authentication.getName()).thenReturn(username);
        when(usuarioService.getByNombreUsuario(username)).thenReturn(java.util.Optional.of(new Usuario()));
        when(rateLimiterService.resolveBucket(username)).thenReturn(bucket);
        when(bucket.tryConsume(1)).thenReturn(true);
        when(contaminacionService.obtenerCalidadAire(ciudad, username)).thenReturn(mockResponse);


        ResponseEntity<?> response = contaminacionController.obtenerCalidadAire(ciudad, authentication);


        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(mockResponse, response.getBody());

        verify(contaminacionService).obtenerCalidadAire(ciudad, username);
    }

    @Test
    void limiteDeConsultasAlcanzado() {

        String ciudad = "Bogotá";
        String username = "testUser";

        when(authentication.getName()).thenReturn(username);
        when(usuarioService.getByNombreUsuario(username)).thenReturn(java.util.Optional.of(new Usuario()));
        when(rateLimiterService.resolveBucket(username)).thenReturn(bucket);
        when(bucket.tryConsume(1)).thenReturn(false);


        ResponseEntity<?> response = contaminacionController.obtenerCalidadAire(ciudad, authentication);


        assertNotNull(response);
        assertEquals(429, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof Mensaje);
        assertEquals("Límite de consultas alcanzado. Intenta de nuevo en una hora.", ((Mensaje) response.getBody()).getMensaje());

        verify(contaminacionService, never()).obtenerCalidadAire(anyString(), anyString());
    }

    @Test
    void errorCuandoServicioFalla() {

        String ciudad = "Bogotá";
        String username = "testUser";

        when(authentication.getName()).thenReturn(username);
        when(usuarioService.getByNombreUsuario(username)).thenReturn(java.util.Optional.of(new Usuario()));
        when(rateLimiterService.resolveBucket(username)).thenReturn(bucket);
        when(bucket.tryConsume(1)).thenReturn(true);
        when(contaminacionService.obtenerCalidadAire(ciudad, username)).thenThrow(new RuntimeException("Error interno"));


        ResponseEntity<?> response = contaminacionController.obtenerCalidadAire(ciudad, authentication);


        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof Mensaje);
        assertTrue(((Mensaje) response.getBody()).getMensaje().contains("Error interno"));

        verify(contaminacionService).obtenerCalidadAire(ciudad, username);
    }
}
