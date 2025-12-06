package com.afriasdev.cmms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianPerformanceDTO {
    private Long technicianId;
    private String technicianName;

    private Integer totalWorkOrders;
    private Integer completedWorkOrders;
    private Integer openWorkOrders;
    private Integer overdueWorkOrders;

    private BigDecimal avgCompletionTime;
    private BigDecimal totalHoursWorked;

    private Double completionRate;
    private Double onTimeRate;

    private BigDecimal totalRevenue;
}
