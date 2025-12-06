package com.afriasdev.cmms.dto.request;

import com.afriasdev.cmms.model.WorkOrderPriority;
import com.afriasdev.cmms.model.WorkOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderFilterRequest {

    private List<WorkOrderStatus> statuses;
    private List<WorkOrderPriority> priorities;

    private Long companyId;
    private Long siteId;
    private Long assetId;
    private Long assignedTechId;
    private Long requesterId;

    private LocalDate dueDateFrom;
    private LocalDate dueDateTo;

    private LocalDate createdFrom;
    private LocalDate createdTo;

    private Boolean overdue;
    private Boolean unassigned;

    private String search; // Buscar en título o descripción

    // Paginación
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}