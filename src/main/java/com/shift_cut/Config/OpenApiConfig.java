package com.shift_cut.Config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Shift Cut API")
                        .description("API REST para gestión de turnos de barbería. Incluye autenticación JWT, roles (ADMIN, USER, BARBER), endpoints protegidos y documentación interactiva con Swagger UI.\n\n" +
                                "- Registro y login de usuarios (JWT)\n" +
                                "- Gestión de usuarios y turnos\n" +
                                "- Seguridad por roles\n" +
                                "- Usuario ADMIN creado automáticamente la primera vez\n\n" +
                                "**IMPORTANTE:** Para probar endpoints protegidos, primero obtené un token en /auth/login y usalo en el botón Authorize de Swagger UI.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Ciro - Shift Cut Dev")
                                .email("admin@admin.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Ingresa el token JWT obtenido en /auth/login. Ejemplo: eyJhbGci...")));
    }
}
