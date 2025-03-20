package com.tiempo.clima.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API del Clima")
                        .version("1.0")
                        .description("API para obtener el clima actual,el pronósticos del clima y calidad del aire"))
                .addSecurityItem(new SecurityRequirement().addList("autenticaciónBearer"))
                .components(new Components()
                        .addSecuritySchemes("autenticaciónBearer", new SecurityScheme()
                                .name("autenticaciónBearer")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .tags(List.of(
                        new Tag().name("1. Autenticación").description("Endpoints para registro y autenticación"),
                        new Tag().name("2. Clima").description("Endpoints para el clima actual"),
                        new Tag().name("3. Contaminación").description("Endpoints para la contaminación"),
                        new Tag().name("4. Pronóstico").description("Endpoints para el pronóstico"),
                        new Tag().name("5. Caché").description("Endpoints para la caché"),
                        new Tag().name("6. Consultas").description("Endpoints para consultar el historial de búsqueda")
                ));
    }
}
