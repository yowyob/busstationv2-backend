package cm.yowyob.bus_station_backend.infrastructure.config.web;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {
        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("BusStation API (Reactive)")
                                                .version("1.0")
                                                .contact(new Contact()
                                                                .name("Tchassi Daniel et FOMEKONG Jonathan")
                                                                .email("tchassidaniel@gmail.com, jonathabachelard@gmail.com"))
                                                .description("API Architecture Hexagonale & WebFlux"))
                                .addServersItem(new Server()
                                                .url("https://traefikdev.yowyob.com/bus-station")
                                                .description("Production"))
                                .addServersItem(new Server()
                                                .url("http://localhost:8080")
                                                .description("En local"))
                                .components(new Components()
                                                .addSecuritySchemes("bearerAuth",
                                                                new SecurityScheme()
                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                .scheme("bearer")
                                                                                .bearerFormat("JWT")));
        }
}
