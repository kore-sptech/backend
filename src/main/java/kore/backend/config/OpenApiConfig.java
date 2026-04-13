package kore.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenApi(){
        return new OpenAPI()
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
