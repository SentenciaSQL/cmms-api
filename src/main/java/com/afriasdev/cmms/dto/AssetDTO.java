package com.afriasdev.cmms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetDTO {
    private Long id;

    @NotNull(message = "El ID del sitio es requerido")
    private Long siteId;

    private String siteName;
    private String companyName;

    private Long categoryId;
    private String categoryName;

    @NotBlank(message = "El código del activo es requerido")
    @Size(max = 100, message = "El código no puede exceder 100 caracteres")
    private String code;

    @NotBlank(message = "El nombre del activo es requerido")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    private String name;

    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String description;

    @Size(max = 100, message = "El número de serie no puede exceder 100 caracteres")
    private String serialNumber;

    @Size(max = 100, message = "El fabricante no puede exceder 100 caracteres")
    private String manufacturer;

    @Size(max = 100, message = "El modelo no puede exceder 100 caracteres")
    private String model;

    private LocalDate installedAt;

    private Boolean isActive;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Métricas
    private Integer totalWorkOrders;
    private Integer openWorkOrders;
    private LocalDateTime lastMaintenanceDate;
}
