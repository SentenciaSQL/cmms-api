package com.afriasdev.cmms.dto;

import com.afriasdev.cmms.model.WorkOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderHistoryDTO {
    private Long id;
    private Long workOrderId;
    private WorkOrderStatus oldStatus;
    private WorkOrderStatus newStatus;
    private String changedBy;
    private String notes;
    private LocalDateTime changedAt;
}
