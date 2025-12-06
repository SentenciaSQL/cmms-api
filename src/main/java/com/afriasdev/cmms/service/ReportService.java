package com.afriasdev.cmms.service;

import com.afriasdev.cmms.dto.AssetMaintenanceHistoryDTO;
import com.afriasdev.cmms.dto.MonthlyReportDTO;
import com.afriasdev.cmms.dto.TechnicianPerformanceDTO;
import com.afriasdev.cmms.model.WorkOrder;
import com.afriasdev.cmms.model.WorkOrderPriority;
import com.afriasdev.cmms.model.WorkOrderStatus;
import com.afriasdev.cmms.repository.AssetRepository;
import com.afriasdev.cmms.repository.TechnicianRepository;
import com.afriasdev.cmms.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final WorkOrderRepository workOrderRepository;
    private final TechnicianRepository technicianRepository;
    private final AssetRepository assetRepository;

    /**
     * Genera reporte mensual
     */
    public MonthlyReportDTO generateMonthlyReport(YearMonth month) {
        LocalDateTime startOfMonth = month.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = month.atEndOfMonth().atTime(23, 59, 59);

        List<WorkOrder> monthOrders = workOrderRepository.findByCreatedAtBetween(startOfMonth, endOfMonth);

        MonthlyReportDTO report = new MonthlyReportDTO();
        report.setMonth(month);
        report.setTotalWorkOrders((long) monthOrders.size());

        // Contar por estado
        long completed = monthOrders.stream()
                .filter(wo -> wo.getStatus() == WorkOrderStatus.COMPLETED)
                .count();
        report.setCompletedWorkOrders(completed);

        long cancelled = monthOrders.stream()
                .filter(wo -> wo.getStatus() == WorkOrderStatus.CANCELLED)
                .count();
        report.setCancelledWorkOrders(cancelled);

        // Contar por prioridad
        Map<String, Long> byPriority = new HashMap<>();
        for (WorkOrderPriority priority : WorkOrderPriority.values()) {
            long count = monthOrders.stream()
                    .filter(wo -> wo.getPriority() == priority)
                    .count();
            byPriority.put(priority.toString(), count);
        }
        report.setWorkOrdersByPriority(byPriority);

        // Contar por técnico
        Map<String, Long> byTechnician = monthOrders.stream()
                .filter(wo -> wo.getAssignedTech() != null)
                .collect(Collectors.groupingBy(
                        wo -> wo.getAssignedTech().getUser().getFirstName() + " " +
                                wo.getAssignedTech().getUser().getLastName(),
                        Collectors.counting()
                ));
        report.setWorkOrdersByTechnician(byTechnician);

        // Calcular tiempos promedio (solo completadas)
        List<WorkOrder> completedOrders = monthOrders.stream()
                .filter(wo -> wo.getStatus() == WorkOrderStatus.COMPLETED)
                .toList();

        if (!completedOrders.isEmpty()) {
            // Tiempo promedio de completación
            double avgCompletion = completedOrders.stream()
                    .filter(wo -> wo.getActualHours() != null)
                    .mapToDouble(wo -> wo.getActualHours().doubleValue())
                    .average()
                    .orElse(0.0);
            report.setAvgCompletionTime(BigDecimal.valueOf(avgCompletion).setScale(2, RoundingMode.HALF_UP));

            // Tiempo promedio de respuesta
            double avgResponse = completedOrders.stream()
                    .filter(wo -> wo.getStartedAt() != null)
                    .mapToDouble(wo -> ChronoUnit.HOURS.between(wo.getCreatedAt(), wo.getStartedAt()))
                    .average()
                    .orElse(0.0);
            report.setAvgResponseTime(BigDecimal.valueOf(avgResponse).setScale(2, RoundingMode.HALF_UP));
        }

        // Calcular costos
        BigDecimal totalCosts = monthOrders.stream()
                .filter(wo -> wo.getActualHours() != null &&
                        wo.getAssignedTech() != null &&
                        wo.getAssignedTech().getHourlyRate() != null)
                .map(wo -> wo.getActualHours().multiply(wo.getAssignedTech().getHourlyRate()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setTotalCosts(totalCosts);
        report.setTotalLaborCosts(totalCosts);

        // Assets servidos
        Set<Long> uniqueAssets = monthOrders.stream()
                .filter(wo -> wo.getAsset() != null)
                .map(wo -> wo.getAsset().getId())
                .collect(Collectors.toSet());
        report.setAssetsServiced((long) uniqueAssets.size());

        // Asset más servido
        Map<Long, Long> assetCounts = monthOrders.stream()
                .filter(wo -> wo.getAsset() != null)
                .collect(Collectors.groupingBy(
                        wo -> wo.getAsset().getId(),
                        Collectors.counting()
                ));

        if (!assetCounts.isEmpty()) {
            Long mostServicedId = Collections.max(assetCounts.entrySet(), Map.Entry.comparingByValue()).getKey();
            report.setMostServicedAssetId(mostServicedId);

            assetRepository.findById(mostServicedId).ifPresent(asset -> {
                report.setMostServicedAssetName(asset.getName());
            });
        }

        return report;
    }

    /**
     * Genera reporte de desempeño de técnico
     */
    public TechnicianPerformanceDTO generateTechnicianPerformance(Long technicianId) {
        var technician = technicianRepository.findById(technicianId)
                .orElseThrow(() -> new RuntimeException("Técnico no encontrado"));

        List<WorkOrder> techOrders = workOrderRepository.findByAssignedTechId(technicianId);

        TechnicianPerformanceDTO performance = new TechnicianPerformanceDTO();
        performance.setTechnicianId(technicianId);
        performance.setTechnicianName(technician.getUser().getFirstName() + " " +
                technician.getUser().getLastName());

        performance.setTotalWorkOrders(techOrders.size());

        // Contar por estado
        int completed = (int) techOrders.stream()
                .filter(wo -> wo.getStatus() == WorkOrderStatus.COMPLETED)
                .count();
        performance.setCompletedWorkOrders(completed);

        int open = (int) techOrders.stream()
                .filter(wo -> wo.getStatus() == WorkOrderStatus.OPEN ||
                        wo.getStatus() == WorkOrderStatus.IN_PROGRESS)
                .count();
        performance.setOpenWorkOrders(open);

        int overdue = (int) techOrders.stream()
                .filter(wo -> wo.getDueDate() != null &&
                        wo.getDueDate().isBefore(LocalDate.now()) &&
                        wo.getStatus() != WorkOrderStatus.COMPLETED &&
                        wo.getStatus() != WorkOrderStatus.CANCELLED)
                .count();
        performance.setOverdueWorkOrders(overdue);

        // Calcular tiempo promedio
        List<WorkOrder> completedOrders = techOrders.stream()
                .filter(wo -> wo.getStatus() == WorkOrderStatus.COMPLETED)
                .toList();

        if (!completedOrders.isEmpty()) {
            double avgTime = completedOrders.stream()
                    .filter(wo -> wo.getActualHours() != null)
                    .mapToDouble(wo -> wo.getActualHours().doubleValue())
                    .average()
                    .orElse(0.0);
            performance.setAvgCompletionTime(BigDecimal.valueOf(avgTime).setScale(2, RoundingMode.HALF_UP));
        }

        // Total de horas trabajadas
        BigDecimal totalHours = techOrders.stream()
                .filter(wo -> wo.getActualHours() != null)
                .map(WorkOrder::getActualHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        performance.setTotalHoursWorked(totalHours);

        // Tasa de completación
        if (techOrders.size() > 0) {
            double completionRate = (completed * 100.0) / techOrders.size();
            performance.setCompletionRate(completionRate);
        }

        // Tasa de entregas a tiempo
        if (completed > 0) {
            long onTime = completedOrders.stream()
                    .filter(wo -> wo.getDueDate() != null &&
                            wo.getCompletedAt() != null &&
                            !wo.getCompletedAt().toLocalDate().isAfter(wo.getDueDate()))
                    .count();
            double onTimeRate = (onTime * 100.0) / completed;
            performance.setOnTimeRate(onTimeRate);
        }

        // Revenue total
        if (technician.getHourlyRate() != null) {
            BigDecimal revenue = totalHours.multiply(technician.getHourlyRate());
            performance.setTotalRevenue(revenue);
        }

        return performance;
    }

    /**
     * Genera historial de mantenimiento de un activo
     */
    public List<AssetMaintenanceHistoryDTO> generateAssetMaintenanceHistory(Long assetId) {
        List<WorkOrder> assetOrders = workOrderRepository.findByAssetId(assetId);

        return assetOrders.stream()
                .filter(wo -> wo.getStatus() == WorkOrderStatus.COMPLETED)
                .sorted(Comparator.comparing(WorkOrder::getCompletedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(wo -> {
                    AssetMaintenanceHistoryDTO dto = new AssetMaintenanceHistoryDTO();
                    dto.setWorkOrderId(wo.getId());
                    dto.setWorkOrderCode(wo.getCode());
                    dto.setTitle(wo.getTitle());
                    dto.setStatus(wo.getStatus());
                    dto.setCompletedAt(wo.getCompletedAt());
                    dto.setActualHours(wo.getActualHours());
                    dto.setDescription(wo.getDescription());

                    if (wo.getAssignedTech() != null) {
                        dto.setTechnicianName(wo.getAssignedTech().getUser().getFirstName() + " " +
                                wo.getAssignedTech().getUser().getLastName());

                        if (wo.getActualHours() != null && wo.getAssignedTech().getHourlyRate() != null) {
                            dto.setCost(wo.getActualHours().multiply(wo.getAssignedTech().getHourlyRate()));
                        }
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Genera comparación de técnicos
     */
    public List<TechnicianPerformanceDTO> compareTechnicians() {
        return technicianRepository.findByIsActiveTrue().stream()
                .map(tech -> generateTechnicianPerformance(tech.getId()))
                .sorted(Comparator.comparing(TechnicianPerformanceDTO::getCompletedWorkOrders).reversed())
                .collect(Collectors.toList());
    }
}