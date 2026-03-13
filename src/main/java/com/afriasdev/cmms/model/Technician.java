package com.afriasdev.cmms.model;

import com.afriasdev.cmms.model.AuditableEntity;

import com.afriasdev.cmms.security.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "technicians")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Technician extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "skill_level", length = 50)
    private String skillLevel; // junior, semi, senior

    @Column(name = "hourly_rate", precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Column(name = "phone_alt", length = 50)
    private String phoneAlt;

    @Column(length = 255)
    private String notes;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    
    // Relaciones
    @OneToMany(mappedBy = "assignedTech")
    private Set<WorkOrder> workOrders = new HashSet<>();
}
