package com.afriasdev.cmms.dto;

import com.afriasdev.cmms.model.WorkOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetMaintenanceHistoryDTO {
    private Long workOrderId;
    private String workOrderCode;
    private String title;
    private WorkOrderStatus status;
    private String technicianName;
    private LocalDateTime completedAt;
    private BigDecimal actualHours;
    private BigDecimal cost;
    private String description;
}
