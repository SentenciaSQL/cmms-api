package com.afriasdev.cmms.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemType itemType; // SPARE_PART, TOOL, CONSUMABLE, MATERIAL

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(nullable = false)
    private Integer currentStock = 0;

    @Column(nullable = false)
    private Integer minStock = 0;

    @Column(nullable = false)
    private Integer maxStock = 100;

    @Column(nullable = false)
    private Integer reorderPoint = 10;

    @Column(nullable = false)
    private String unit; // UNIT, KG, LITER, METER, etc.

    @Column(precision = 10, scale = 2)
    private BigDecimal unitCost;

    private String location; // Ubicación en almacén

    private String manufacturer;

    private String partNumber;

    @Column(length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private Boolean isActive = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ItemType {
        SPARE_PART, TOOL, CONSUMABLE, MATERIAL
    }

    public boolean isLowStock() {
        return currentStock <= minStock;
    }

    public boolean needsReorder() {
        return currentStock <= reorderPoint;
    }
}

