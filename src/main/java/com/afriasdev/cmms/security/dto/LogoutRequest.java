package com.afriasdev.cmms.security.dto;

import lombok.Data;

@Data
public class LogoutRequest {

    /**
     * Si se incluye, solo revoca esa sesión.
     * Si es null, se revocan todas las sesiones del usuario autenticado.
     */
    private String refreshToken;
}
