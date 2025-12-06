package com.afriasdev.cmms.dto.request;

import com.afriasdev.cmms.model.InventoryItem;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemCreateRequest {

    @NotBlank(message = "El código es requerido")
    private String code;

    @NotBlank(message = "El nombre es requerido")
    private String name;

    private String description;

    @NotNull(message = "El tipo de ítem es requerido")
    private InventoryItem.ItemType itemType;

    private Long supplierId;

    @NotNull(message = "El stock actual es requerido")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer currentStock;

    @NotNull(message = "El stock mínimo es requerido")
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer minStock;

    @NotNull(message = "El stock máximo es requerido")
    @Min(value = 1, message = "El stock máximo debe ser mayor a 0")
    private Integer maxStock;

    @NotNull(message = "El punto de reorden es requerido")
    @Min(value = 0, message = "El punto de reorden no puede ser negativo")
    private Integer reorderPoint;

    @NotBlank(message = "La unidad es requerida")
    private String unit;

    @DecimalMin(value = "0.0", message = "El costo no puede ser negativo")
    private BigDecimal unitCost;

    private String location;
    private String manufacturer;
    private String partNumber;
    private String imageUrl;
}

