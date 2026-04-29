package kore.backend.config;

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

        public final static String SECURITY_SCHEMA_NAME = "bearerAuth";

        @Bean
        public OpenAPI customOpenApi() {
                return new OpenAPI()
                                .addSecurityItem(
                                                new SecurityRequirement().addList(OpenApiConfig.SECURITY_SCHEMA_NAME))
                                .components(
                                                new Components()
                                                                .addSecuritySchemes(OpenApiConfig.SECURITY_SCHEMA_NAME,
                                                                                new SecurityScheme()
                                                                                                .name(OpenApiConfig.SECURITY_SCHEMA_NAME)
                                                                                                .description(
                                                                                                                "Token de autenticaçao no formato JWT ")
                                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                                .scheme("bearer")
                                                                                                .bearerFormat("JWT")))
                                .info(new Info()
                                                .title("API Gestor Ink")
                                                .version("1.0.0")
                                                .description("Projeto: GestorInk. Desenvolvedores da API: Henry Franz e Vitor Restini")
                                                .contact(new Contact()
                                                                .name("Kore")
                                                                .email("vitor.restini@sptech.school ||  henry.arcaya@sptech.school"))
                                                .license(new License()
                                                                .name("X")));
        };
}
