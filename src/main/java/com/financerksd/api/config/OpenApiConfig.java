package com.financerksd.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

// Agrega el boton "Authorize" en Swagger para pegar el token JWT.
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI financerOpenApi() {
        return new OpenAPI()
                .info(new Info().title("Financer KSD API").version("1.0")
                        .description("Asesoria financiera: diagnosticos, planes de mejora y seguimiento."))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }
}
