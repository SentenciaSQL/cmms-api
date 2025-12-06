package com.afriasdev.cmms.dto;

import com.afriasdev.cmms.WorkOrderEvidenceDTO;
import com.afriasdev.cmms.model.WorkOrderPriority;
import com.afriasdev.cmms.model.WorkOrderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderDTO {

    private Long id;

    private String code; // Auto-generado

    @NotBlank(message = "El título es requerido")
    @Size(max = 200, message = "El título no puede exceder 200 caracteres")
    private String title;

    private String description;

    private Long companyId;
    private String companyName;

    private Long siteId;
    private String siteName;

    private Long assetId;
    private String assetCode;
    private String assetName;

    private Long requesterId;
    private String requesterName;
    private String requesterEmail;

    private Long assignedTechId;
    private String assignedTechName;

    @NotNull(message = "El estado es requerido")
    private WorkOrderStatus status;

    @NotNull(message = "La prioridad es requerida")
    private WorkOrderPriority priority;

    private LocalDate dueDate;
    private LocalDateTime scheduledStart;
    private LocalDateTime scheduledEnd;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    private BigDecimal estimatedHours;
    private BigDecimal actualHours;

    private LocalDateTime createdAt;
    private Long createdById;
    private String createdByName;

    private LocalDateTime updatedAt;
    private Long updatedById;
    private String updatedByName;

    // Lista de evidencias
    private List<WorkOrderEvidenceDTO> evidences;

    // Cálculos
    private BigDecimal estimatedCost;
    private BigDecimal actualCost;
    private Boolean isOverdue;
    private Long daysOpen;
}
