package com.afriasdev.cmms.dto;

import com.afriasdev.cmms.model.WorkOrderPriority;
import com.afriasdev.cmms.model.WorkOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderSummaryDTO {
    private Long id;
    private String code;
    private String title;
    private WorkOrderStatus status;
    private WorkOrderPriority priority;
    private LocalDate dueDate;
    private Boolean isOverdue;

    // Información básica
    private String companyName;
    private String siteName;
    private String assetName;
    private String assignedTechName;

    private LocalDateTime createdAt;
    private Long daysOpen;
}
