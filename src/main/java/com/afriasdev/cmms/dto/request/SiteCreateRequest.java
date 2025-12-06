package com.afriasdev.cmms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SiteCreateRequest {
    @NotNull(message = "El ID de la empresa es requerido")
    private Long companyId;

    @NotBlank(message = "El nombre del sitio es requerido")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    private String name;

    @Size(max = 50, message = "El código no puede exceder 50 caracteres")
    private String code;

    @Size(max = 255, message = "La dirección no puede exceder 255 caracteres")
    private String address;

    @Size(max = 100, message = "La ciudad no puede exceder 100 caracteres")
    private String city;

    @Size(max = 100, message = "El estado/provincia no puede exceder 100 caracteres")
    private String state;

    @Size(max = 100, message = "El país no puede exceder 100 caracteres")
    private String country;

    private Boolean isActive;
}
