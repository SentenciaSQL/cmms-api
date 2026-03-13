package com.afriasdev.cmms.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshRequest {

    @NotBlank(message = "El refresh token no puede estar vacío")
    private String refreshToken;
}
