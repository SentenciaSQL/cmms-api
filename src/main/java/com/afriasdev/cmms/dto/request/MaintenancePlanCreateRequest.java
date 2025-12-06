package com.afriasdev.cmms.dto.request;

import com.afriasdev.cmms.model.MaintenancePlan;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenancePlanCreateRequest {

    @NotBlank(message = "El nombre es requerido")
    private String name;

    private String description;

    @NotNull(message = "El activo es requerido")
    private Long assetId;

    @NotNull(message = "El tipo de mantenimiento es requerido")
    private MaintenancePlan.MaintenanceType type;

    @NotNull(message = "La frecuencia es requerida")
    private MaintenancePlan.FrequencyType frequency;

    @NotNull(message = "El valor de frecuencia es requerido")
    @Min(value = 1, message = "El valor debe ser mayor a 0")
    private Integer frequencyValue;

    private LocalDateTime nextScheduledDate;

    @NotNull(message = "La duración estimada es requerida")
    @Min(value = 1, message = "La duración debe ser mayor a 0")
    private Integer estimatedDurationMinutes;

    @NotNull(message = "La prioridad es requerida")
    private MaintenancePlan.Priority priority;

    private String instructions;

    private Long assignedTechnicianId;

    private Boolean autoGenerateWorkOrder = true;
}

