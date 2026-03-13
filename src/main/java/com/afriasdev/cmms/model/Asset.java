package com.afriasdev.cmms.model;

import com.afriasdev.cmms.model.AuditableEntity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "assets",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_assets_site_code",
                columnNames = {"site_id", "code"}
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Asset extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private AssetCategory category;

    @Column(nullable = false, length = 100)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(length = 100)
    private String manufacturer;

    @Column(length = 100)
    private String model;

    @Column(name = "installed_at")
    private LocalDate installedAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    
    // Relaciones
    @OneToMany(mappedBy = "asset")
    private Set<WorkOrder> workOrders = new HashSet<>();
}
