package com.afriasdev.cmms.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCreateRequest {

    @NotNull(message = "El ID de usuario es requerido")
    private Long userId;

    private Long companyId;

    @Size(max = 100, message = "El cargo no puede exceder 100 caracteres")
    private String position;

    @Size(max = 50, message = "El teléfono alternativo no puede exceder 50 caracteres")
    private String phoneAlt;

    @Size(max = 255, message = "Las notas no pueden exceder 255 caracteres")
    private String notes;

    private Boolean isActive;
}