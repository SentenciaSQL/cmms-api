package com.afriasdev.cmms.service;

import com.afriasdev.cmms.dto.DashboardStatsDTO;
import com.afriasdev.cmms.model.WorkOrderPriority;
import com.afriasdev.cmms.model.WorkOrderStatus;
import com.afriasdev.cmms.repository.AssetRepository;
import com.afriasdev.cmms.repository.TechnicianRepository;
import com.afriasdev.cmms.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final WorkOrderRepository workOrderRepository;
    private final AssetRepository assetRepository;
    private final TechnicianRepository technicianRepository;

    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        // Work Orders por estado
        stats.setTotalWorkOrders(workOrderRepository.count());
        stats.setOpenWorkOrders(workOrderRepository.countByStatus(WorkOrderStatus.OPEN));
        stats.setInProgressWorkOrders(workOrderRepository.countByStatus(WorkOrderStatus.IN_PROGRESS));
        stats.setCompletedWorkOrders(workOrderRepository.countByStatus(WorkOrderStatus.COMPLETED));

        // Work Orders vencidas
        List<WorkOrderStatus> activeStatuses = Arrays.asList(WorkOrderStatus.OPEN, WorkOrderStatus.IN_PROGRESS);
        long overdueCount = workOrderRepository.findOverdueWorkOrders(LocalDate.now()).size();
        stats.setOverdueWorkOrders(overdueCount);

        // Work Orders por prioridad
        stats.setUrgentWorkOrders(workOrderRepository.countByPriority(WorkOrderPriority.URGENT));
        stats.setHighPriorityWorkOrders(workOrderRepository.countByPriority(WorkOrderPriority.HIGH));
        stats.setMediumPriorityWorkOrders(workOrderRepository.countByPriority(WorkOrderPriority.MEDIUM));
        stats.setLowPriorityWorkOrders(workOrderRepository.countByPriority(WorkOrderPriority.LOW));

        // Assets
        stats.setTotalAssets(assetRepository.count());
        stats.setActiveAssets(assetRepository.findByIsActiveTrue().stream().count());

        // Técnicos
        stats.setTotalTechnicians(technicianRepository.count());
        stats.setAvailableTechnicians((long) technicianRepository.findAvailableTechnicians(5).size());
        long busyTechs = stats.getTotalTechnicians() - stats.getAvailableTechnicians();
        stats.setBusyTechnicians(busyTechs > 0 ? busyTechs : 0L);

        // Métricas de tiempo y costos
        calculateTimeMetrics(stats);
        calculateCostMetrics(stats);

        return stats;
    }

    private void calculateTimeMetrics(DashboardStatsDTO stats) {
        // Obtener OT completadas del último mes para calcular promedios
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        var completedOrders = workOrderRepository.findByStatusIn(Arrays.asList(WorkOrderStatus.COMPLETED))
                .stream()
                .filter(wo -> wo.getCompletedAt() != null && wo.getCompletedAt().isAfter(oneMonthAgo))
                .toList();

        if (!completedOrders.isEmpty()) {
            // Tiempo promedio de completación
            double avgHours = completedOrders.stream()
                    .filter(wo -> wo.getActualHours() != null)
                    .mapToDouble(wo -> wo.getActualHours().doubleValue())
                    .average()
                    .orElse(0.0);

            stats.setAvgCompletionTime(BigDecimal.valueOf(avgHours));

            // Tiempo promedio de respuesta (desde creación hasta inicio)
            double avgResponseHours = completedOrders.stream()
                    .filter(wo -> wo.getStartedAt() != null)
                    .mapToDouble(wo -> {
                        long hours = java.time.temporal.ChronoUnit.HOURS
                                .between(wo.getCreatedAt(), wo.getStartedAt());
                        return hours;
                    })
                    .average()
                    .orElse(0.0);

            stats.setAvgResponseTime(BigDecimal.valueOf(avgResponseHours));
        }
    }

    private void calculateCostMetrics(DashboardStatsDTO stats) {
        // Costos del mes actual
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        var monthOrders = workOrderRepository.findByCreatedAtBetween(startOfMonth, endOfMonth);

        BigDecimal monthlyCost = monthOrders.stream()
                .filter(wo -> wo.getActualHours() != null &&
                        wo.getAssignedTech() != null &&
                        wo.getAssignedTech().getHourlyRate() != null)
                .map(wo -> wo.getActualHours().multiply(wo.getAssignedTech().getHourlyRate()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        stats.setTotalCostsThisMonth(monthlyCost);

        // Costos del año actual
        LocalDateTime startOfYear = LocalDateTime.now().withDayOfYear(1).withHour(0).withMinute(0);
        LocalDateTime endOfYear = LocalDateTime.now().withDayOfYear(1).plusYears(1).minusDays(1).withHour(23).withMinute(59);

        var yearOrders = workOrderRepository.findByCreatedAtBetween(startOfYear, endOfYear);

        BigDecimal yearlyCost = yearOrders.stream()
                .filter(wo -> wo.getActualHours() != null &&
                        wo.getAssignedTech() != null &&
                        wo.getAssignedTech().getHourlyRate() != null)
                .map(wo -> wo.getActualHours().multiply(wo.getAssignedTech().getHourlyRate()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        stats.setTotalCostsThisYear(yearlyCost);
    }
}