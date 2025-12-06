package com.afriasdev.cmms.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianCreateRequest {

    @NotNull(message = "El ID de usuario es requerido")
    private Long userId;

    @Size(max = 50, message = "El nivel de habilidad no puede exceder 50 caracteres")
    private String skillLevel; // junior, semi, senior

    @DecimalMin(value = "0.0", inclusive = false, message = "La tarifa debe ser mayor a 0")
    private BigDecimal hourlyRate;

    @Size(max = 50, message = "El teléfono alternativo no puede exceder 50 caracteres")
    private String phoneAlt;

    @Size(max = 255, message = "Las notas no pueden exceder 255 caracteres")
    private String notes;

    private Boolean isActive;
}
