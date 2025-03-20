package com.tiempo.clima.dto;

import lombok.Data;

import java.util.List;

@Data
public class OpenWeatherMapPronosticoResponse {
    private List<PronosticoData> list;

    @Data
    public static class PronosticoData {
        private Main main;
        private List<Weather> weather;
        private Wind wind;
        private long dt;
    }

    @Data
    public static class Main {
        private double temp;
        private int humidity;
        private double pressure;
        private double feelsLike;
    }

    @Data
    public static class Weather {
        private String description;
        private String icon;
    }

    @Data
    public static class Wind {
        private double speed;
    }
}
