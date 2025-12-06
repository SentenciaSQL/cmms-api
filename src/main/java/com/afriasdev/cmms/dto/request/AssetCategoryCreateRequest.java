package com.afriasdev.cmms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetCategoryCreateRequest {

    @NotBlank(message = "El nombre de la categoría es requerido")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String name;

    @Size(max = 50, message = "El código no puede exceder 50 caracteres")
    private String code;

    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String description;

    private Boolean isActive;
}