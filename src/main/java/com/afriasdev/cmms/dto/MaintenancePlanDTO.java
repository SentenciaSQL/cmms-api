package com.afriasdev.cmms.dto;

import com.afriasdev.cmms.model.MaintenancePlan;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenancePlanDTO {

    private Long id;
    private String name;
    private String description;
    private AssetDTO asset;
    private MaintenancePlan.MaintenanceType type;
    private MaintenancePlan.FrequencyType frequency;
    private Integer frequencyValue;
    private LocalDateTime nextScheduledDate;
    private LocalDateTime lastExecutionDate;
    private Integer estimatedDurationMinutes;
    private MaintenancePlan.Priority priority;
    private String instructions;
    private TechnicianDTO assignedTechnician;
    private Boolean isActive;
    private Boolean autoGenerateWorkOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
