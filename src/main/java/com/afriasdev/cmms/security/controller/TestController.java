package com.afriasdev.cmms.security.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    // Endpoint público
    @GetMapping("/public")
    public String publicEndpoint() {
        return "Este endpoint es público. No necesitas login.";
    }

    // Endpoint protegido (requiere JWT)
    @GetMapping("/private")
    public String privateEndpoint() {
        return "Acceso concedido. Tu JWT es válido.";
    }
}
