package com.tiempo.clima.service;

import com.tiempo.clima.dto.ClimaResponse;
import com.tiempo.clima.entity.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class ClimaService {

    @Value("${api.weather.url}")
    private String apiUrl;

    @Value("${openweathermap.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ConsultaService consultaService;
    private final UsuarioService usuarioService;

    public ClimaService(RestTemplate restTemplate, ConsultaService consultaService, UsuarioService usuarioService) {
        this.restTemplate = restTemplate;
        this.consultaService = consultaService;
        this.usuarioService = usuarioService;
    }

    @Cacheable(value = "clima", key = "#ciudad")
    public ClimaResponse obtenerClima(String ciudad, String nombreUsuario) {
        String url = String.format("%s?q=%s&appid=%s&units=metric&lang=es", apiUrl, ciudad, apiKey);
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response == null) {
            throw new RuntimeException("No se pudieron obtener datos del clima");
        }

        Map<String, Object> main = (Map<String, Object>) response.get("main");
        double temperatura = (double) main.get("temp");
        double tempMin = (double) main.get("temp_min");
        double tempMax = (double) main.get("temp_max");
        double sensacionTermica = (double) main.get("feels_like");

        List<Map<String, Object>> weatherList = (List<Map<String, Object>>) response.get("weather");
        String descripcion = weatherList.get(0).get("description").toString();

        String descripcionTraducida = traducirDescripcion(descripcion);

        Usuario usuario = usuarioService.getByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String resultado = String.format("Clima actual: %s, %.1f°C", descripcionTraducida, temperatura);

        consultaService.registrarConsulta(usuario, ciudad, resultado, "Consulta de clima actual");

        return new ClimaResponse(
                ciudad,
                temperatura,
                tempMin,
                tempMax,
                sensacionTermica,
                descripcionTraducida
        );
    }

    private String traducirDescripcion(String descripcion) {
        return switch (descripcion.toLowerCase()) {
            case "clear sky" -> "Cielo despejado";
            case "few clouds" -> "Pocas nubes";
            case "scattered clouds" -> "Nubes dispersas";
            case "broken clouds" -> "Nublado";
            case "shower rain" -> "Lluvia ligera";
            case "rain" -> "Lluvia";
            case "thunderstorm" -> "Tormenta";
            case "snow" -> "Nieve";
            case "mist" -> "Neblina";
            case "overcast clouds" -> "Muy nuboso";
            default -> descripcion;
        };
    }
}
