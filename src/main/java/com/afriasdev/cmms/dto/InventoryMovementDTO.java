package com.afriasdev.cmms.dto;

import com.afriasdev.cmms.model.InventoryMovement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovementDTO {

    private Long id;
    private InventoryItemDTO inventoryItem;
    private InventoryMovement.MovementType movementType;
    private Integer quantity;
    private Integer previousStock;
    private Integer newStock;
    private BigDecimal unitCost;
    private BigDecimal totalCost;
    private WorkOrderDTO workOrder;
    private UserDTO user;
    private String notes;
    private String referenceNumber;
    private LocalDateTime movementDate;
    private LocalDateTime createdAt;
}
