package com.afriasdev.cmms.controller;

import com.afriasdev.cmms.dto.AssetMaintenanceHistoryDTO;
import com.afriasdev.cmms.dto.MonthlyReportDTO;
import com.afriasdev.cmms.dto.TechnicianPerformanceDTO;
import com.afriasdev.cmms.repository.AssetRepository;
import com.afriasdev.cmms.service.ExportService;
import com.afriasdev.cmms.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Reports", description = "Gestión de Reportes y Análisis de Desempeño")
public class ReportController {

    private final ReportService reportService;
    private final ExportService exportService;
    private final AssetRepository assetRepository;

    /**
     * Obtener reporte mensual
     * Ejemplo: GET /api/reports/monthly?month=2024-03
     */
    @GetMapping("/monthly")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<MonthlyReportDTO> getMonthlyReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        MonthlyReportDTO report = reportService.generateMonthlyReport(month);
        return ResponseEntity.ok(report);
    }

    /**
     * Obtener reporte mensual del mes actual
     */
    @GetMapping("/monthly/current")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<MonthlyReportDTO> getCurrentMonthReport() {
        YearMonth currentMonth = YearMonth.now();
        MonthlyReportDTO report = reportService.generateMonthlyReport(currentMonth);
        return ResponseEntity.ok(report);
    }

    /**
     * Obtener desempeño de un técnico
     */
    @GetMapping("/technician/{technicianId}/performance")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<TechnicianPerformanceDTO> getTechnicianPerformance(@PathVariable Long technicianId) {
        TechnicianPerformanceDTO performance = reportService.generateTechnicianPerformance(technicianId);
        return ResponseEntity.ok(performance);
    }

    /**
     * Comparar desempeño de todos los técnicos
     */
    @GetMapping("/technicians/comparison")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<TechnicianPerformanceDTO>> compareTechnicians() {
        List<TechnicianPerformanceDTO> comparison = reportService.compareTechnicians();
        return ResponseEntity.ok(comparison);
    }

    /**
     * Obtener historial de mantenimiento de un activo
     */
    @GetMapping("/asset/{assetId}/maintenance-history")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    public ResponseEntity<List<AssetMaintenanceHistoryDTO>> getAssetMaintenanceHistory(@PathVariable Long assetId) {
        List<AssetMaintenanceHistoryDTO> history = reportService.generateAssetMaintenanceHistory(assetId);
        return ResponseEntity.ok(history);
    }

    // ── EXPORTS: Reporte Mensual ──────────────────────────────────────────

    @GetMapping(value = "/monthly/export/pdf", produces = "application/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<byte[]> exportMonthlyPdf(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        MonthlyReportDTO report = reportService.generateMonthlyReport(month);
        byte[] pdf = exportService.exportMonthlyReportPdf(report);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=reporte-mensual-" + month + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping(value = "/monthly/export/excel",
                produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<byte[]> exportMonthlyExcel(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        MonthlyReportDTO report = reportService.generateMonthlyReport(month);
        byte[] xlsx = exportService.exportMonthlyReportExcel(report);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=reporte-mensual-" + month + ".xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(xlsx);
    }

    // ── EXPORTS: Técnicos ────────────────────────────────────────────────

    @GetMapping(value = "/technicians/export/excel",
                produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<byte[]> exportTechniciansExcel() {
        List<TechnicianPerformanceDTO> data = reportService.compareTechnicians();
        byte[] xlsx = exportService.exportTechnicianComparisonExcel(data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=performance-tecnicos.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(xlsx);
    }

    // ── EXPORTS: Historial de Activo ─────────────────────────────────────

    @GetMapping(value = "/asset/{assetId}/maintenance-history/export/pdf",
                produces = "application/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    public ResponseEntity<byte[]> exportAssetHistoryPdf(@PathVariable Long assetId) {
        List<AssetMaintenanceHistoryDTO> history =
                reportService.generateAssetMaintenanceHistory(assetId);
        String assetName = assetRepository.findById(assetId)
                .map(a -> a.getName()).orElse(null);
        byte[] pdf = exportService.exportAssetHistoryPdf(assetId, assetName, history);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=historial-activo-" + assetId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}