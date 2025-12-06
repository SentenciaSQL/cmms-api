package com.afriasdev.cmms.dto.request;

import com.afriasdev.cmms.model.WorkOrderPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderCreateRequest {

    @NotBlank(message = "El título es requerido")
    @Size(max = 200, message = "El título no puede exceder 200 caracteres")
    private String title;

    private String description;

    private Long companyId;

    private Long siteId;

    private Long assetId;

    private Long assignedTechId;

    @NotNull(message = "La prioridad es requerida")
    private WorkOrderPriority priority;

    private LocalDate dueDate;

    private LocalDateTime scheduledStart;

    private LocalDateTime scheduledEnd;

    private BigDecimal estimatedHours;
}
