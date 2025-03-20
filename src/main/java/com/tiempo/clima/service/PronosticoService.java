package com.tiempo.clima.service;

import com.tiempo.clima.dto.PronosticoDTO;
import com.tiempo.clima.dto.PronosticoResponse;
import com.tiempo.clima.dto.OpenWeatherMapPronosticoResponse;
import com.tiempo.clima.entity.Consulta;
import com.tiempo.clima.entity.Usuario;
import com.tiempo.clima.repository.ConsultaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PronosticoService {

    private final RestTemplate restTemplate;
    private final ConsultaRepository consultaRepository;
    private final UsuarioService usuarioService;

    @Value("${openweathermap.api.key}")
    private String apiKey;

    @Value("${api.forecast.url}")
    private String apiUrl;

    @Cacheable(value = "pronostico", key = "#ciudad")
    public PronosticoResponse obtenerPronostico(String ciudad, String nombreUsuario) {
        List<PronosticoDTO> pronostico = obtenerListaPronostico(ciudad, "metric", "es");

        if (!pronostico.isEmpty()) {
            String resultado = String.format("Pronóstico para %s: %s, %.1f°C",
                    ciudad,
                    pronostico.get(0).getDescripcion(),
                    pronostico.get(0).getTemperatura()
            );
            guardarConsulta(nombreUsuario, ciudad, resultado, "Consulta de pronóstico");
        }

        return new PronosticoResponse(ciudad, pronostico);
    }

    private void guardarConsulta(String nombreUsuario, String ciudad, String resultado, String tipoConsulta) {
        Usuario usuario = usuarioService.getByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Consulta consulta = new Consulta();
        consulta.setUsuario(usuario);
        consulta.setCiudad(ciudad);
        consulta.setResultado(resultado);
        consulta.setFechaConsulta(LocalDateTime.now());
        consulta.setTipoConsulta(tipoConsulta);

        consultaRepository.save(consulta);
    }

    public List<PronosticoDTO> obtenerListaPronostico(String ciudad, String units, String lang) {
        String url = String.format("%s?q=%s&appid=%s&units=%s&lang=%s", apiUrl, ciudad, apiKey, units, lang);
        OpenWeatherMapPronosticoResponse respuesta = restTemplate.getForObject(url, OpenWeatherMapPronosticoResponse.class);

        if (respuesta == null || respuesta.getList() == null) {
            throw new RuntimeException("No se pudo obtener el pronóstico del clima");
        }

        Map<LocalDate, List<OpenWeatherMapPronosticoResponse.PronosticoData>> pronosticoPorDia = respuesta.getList().stream()
                .collect(Collectors.groupingBy(dato -> LocalDate.ofInstant(Instant.ofEpochSecond(dato.getDt()), ZoneId.systemDefault())));

        return pronosticoPorDia.entrySet().stream()
                .limit(5)
                .map(entry -> {
                    List<OpenWeatherMapPronosticoResponse.PronosticoData> datosDelDia = entry.getValue();

                    double temperaturaPromedio = datosDelDia.stream().mapToDouble(d -> d.getMain().getTemp()).average().orElse(0);
                    int humedadPromedio = (int) datosDelDia.stream().mapToInt(d -> d.getMain().getHumidity()).average().orElse(0);
                    double velocidadVientoPromedio = datosDelDia.stream().mapToDouble(d -> d.getWind().getSpeed()).average().orElse(0);
                    double presionPromedio = datosDelDia.stream().mapToDouble(d -> d.getMain().getPressure()).average().orElse(0);

                    String descripcionMasFrecuente = datosDelDia.stream()
                            .collect(Collectors.groupingBy(d -> d.getWeather().get(0).getDescription(), Collectors.counting()))
                            .entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse("");

                    String iconoMasFrecuente = datosDelDia.stream()
                            .collect(Collectors.groupingBy(d -> d.getWeather().get(0).getIcon(), Collectors.counting()))
                            .entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse("");

                    long fechaHora = datosDelDia.get(datosDelDia.size() - 1).getDt();

                    return new PronosticoDTO(
                            ciudad,
                            temperaturaPromedio,
                            traducirDescripcion(descripcionMasFrecuente),
                            humedadPromedio,
                            velocidadVientoPromedio,
                            presionPromedio,
                            iconoMasFrecuente,
                            fechaHora
                    );
                })
                .collect(Collectors.toList());
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
