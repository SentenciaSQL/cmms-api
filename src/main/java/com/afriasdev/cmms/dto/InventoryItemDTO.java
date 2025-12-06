package com.afriasdev.cmms.dto;

import com.afriasdev.cmms.model.InventoryItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private InventoryItem.ItemType itemType;
    private SupplierDTO supplier;
    private Integer currentStock;
    private Integer minStock;
    private Integer maxStock;
    private Integer reorderPoint;
    private String unit;
    private BigDecimal unitCost;
    private String location;
    private String manufacturer;
    private String partNumber;
    private String imageUrl;
    private Boolean isActive;
    private Boolean isLowStock;
    private Boolean needsReorder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

