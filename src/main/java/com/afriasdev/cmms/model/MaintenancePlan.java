package com.afriasdev.cmms.model;

import com.afriasdev.cmms.model.AuditableEntity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenancePlan extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceType type; // PREVENTIVE, PREDICTIVE, CORRECTIVE

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FrequencyType frequency; // DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY

    @Column(nullable = false)
    private Integer frequencyValue; // Cada cuántos días/semanas/meses

    private LocalDateTime nextScheduledDate;

    private LocalDateTime lastExecutionDate;

    @Column(nullable = false)
    private Integer estimatedDurationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(length = 2000)
    private String instructions;

    @ManyToOne
    @JoinColumn(name = "assigned_technician_id")
    private Technician assignedTechnician;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Boolean autoGenerateWorkOrder = true;


    public enum MaintenanceType {
        PREVENTIVE, PREDICTIVE, CORRECTIVE
    }

    public enum FrequencyType {
        DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
    }

    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}
