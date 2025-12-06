package com.afriasdev.cmms.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CMMS API - Sistema de Gestión de Mantenimiento")
                        .version("1.0.0")
                        .description("""
                                API REST completa para un Sistema de Gestión de Mantenimiento Computarizado (CMMS).
                                
                                ## Características principales:
                                
                                - 🏢 Gestión de Empresas y Sitios
                                - 🔧 Gestión de Activos y Equipos
                                - 👷 Gestión de Técnicos
                                - 📋 Gestión de Órdenes de Trabajo
                                - 📊 Dashboard y Reportes
                                - 📁 Gestión de Archivos y Evidencias
                                
                                ## Roles de Usuario:
                                
                                - **ADMIN**: Acceso total al sistema
                                - **MANAGER**: Gestión de órdenes de trabajo, activos y reportes
                                - **TECHNICIAN**: Ver y actualizar órdenes asignadas
                                - **REQUESTER**: Crear solicitudes de mantenimiento
                                
                                ## Autenticación:
                                
                                Todos los endpoints (excepto login) requieren un token JWT.
                                Usa el botón "Authorize" arriba para agregar tu token.
                                """)
                        .contact(new Contact()
                                .name("Equipo CMMS")
                                .email("soporte@cmms.com")
                                .url("https://cmms.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Servidor Local de Desarrollo"),
                        new Server()
                                .url("https://api.cmms.com")
                                .description("Servidor de Producción")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Ingresa tu token JWT (sin 'Bearer ' al inicio)")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}
