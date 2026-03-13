package com.afriasdev.cmms.model;

import com.afriasdev.cmms.model.AuditableEntity;

import com.afriasdev.cmms.security.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_movements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMovement extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItem inventoryItem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementType movementType; // IN, OUT, ADJUSTMENT, TRANSFER

    @Column(nullable = false)
    private Integer quantity;

    private Integer previousStock;

    private Integer newStock;

    @Column(precision = 10, scale = 2)
    private BigDecimal unitCost;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalCost;

    @ManyToOne
    @JoinColumn(name = "work_order_id")
    private WorkOrder workOrder;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 500)
    private String notes;

    @Column(length = 100)
    private String referenceNumber; // Número de factura, orden de compra, etc.

    @Column(nullable = false)
    private LocalDateTime movementDate;

    @PrePersist
    protected void onCreate() {
        if (movementDate == null) {
            movementDate = LocalDateTime.now();
        }
    }

    public enum MovementType {
        IN,          // Entrada (compra, devolución)
        OUT,         // Salida (uso en OT, venta)
        ADJUSTMENT,  // Ajuste de inventario
        TRANSFER     // Transferencia entre ubicaciones
    }
}
