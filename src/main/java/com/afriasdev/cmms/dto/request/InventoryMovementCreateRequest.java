package com.afriasdev.cmms.dto.request;

import com.afriasdev.cmms.model.InventoryMovement;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovementCreateRequest {

    @NotNull(message = "El ítem de inventario es requerido")
    private Long inventoryItemId;

    @NotNull(message = "El tipo de movimiento es requerido")
    private InventoryMovement.MovementType movementType;

    @NotNull(message = "La cantidad es requerida")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer quantity;

    @DecimalMin(value = "0.0", message = "El costo no puede ser negativo")
    private BigDecimal unitCost;

    private Long workOrderId;

    private String notes;

    private String referenceNumber;

    private LocalDateTime movementDate;
}

