package com.restaurant.app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** OpenAPI configuration for Swagger UI documentation. JWT authentication scheme included. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI restaurantOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(
                        new Info()
                                .title("Restaurant Management System API")
                                .description("Spring Boot 3 backend for restaurant operations")
                                .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        securitySchemeName,
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                                .description(
                                                        "JWT token authentication. Use 'Bearer"
                                                            + " {token}' format. Also include"
                                                            + " 'x-restaurant-id' header for tenant"
                                                            + " isolation.")));
    }
}
