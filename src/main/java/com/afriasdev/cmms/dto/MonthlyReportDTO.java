package com.afriasdev.cmms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyReportDTO {
    private YearMonth month;

    private Long totalWorkOrders;
    private Long completedWorkOrders;
    private Long cancelledWorkOrders;

    private Map<String, Long> workOrdersByPriority;
    private Map<String, Long> workOrdersByTechnician;

    private BigDecimal avgCompletionTime;
    private BigDecimal avgResponseTime;

    private BigDecimal totalCosts;
    private BigDecimal totalLaborCosts;

    private Long assetsServiced;
    private Long mostServicedAssetId;
    private String mostServicedAssetName;
}
