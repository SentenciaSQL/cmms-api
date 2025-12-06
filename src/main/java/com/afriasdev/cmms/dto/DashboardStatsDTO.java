package com.afriasdev.cmms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    // Work Orders
    private Long totalWorkOrders;
    private Long openWorkOrders;
    private Long inProgressWorkOrders;
    private Long completedWorkOrders;
    private Long overdueWorkOrders;

    // Por prioridad
    private Long urgentWorkOrders;
    private Long highPriorityWorkOrders;
    private Long mediumPriorityWorkOrders;
    private Long lowPriorityWorkOrders;

    // Assets
    private Long totalAssets;
    private Long activeAssets;
    private Long assetsNeedingMaintenance;

    // Técnicos
    private Long totalTechnicians;
    private Long availableTechnicians;
    private Long busyTechnicians;

    // Métricas de tiempo
    private BigDecimal avgCompletionTime; // en horas
    private BigDecimal avgResponseTime;   // en horas

    // Costos
    private BigDecimal totalCostsThisMonth;
    private BigDecimal totalCostsThisYear;
}
