package com.afriasdev.cmms.service;

import com.afriasdev.cmms.dto.AssetMaintenanceHistoryDTO;
import com.afriasdev.cmms.dto.MonthlyReportDTO;
import com.afriasdev.cmms.dto.TechnicianPerformanceDTO;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Servicio de exportación de reportes a PDF y Excel.
 *
 * Reportes disponibles:
 *  - Reporte mensual        → PDF y Excel
 *  - Performance técnicos   → Excel (tabla comparativa)
 *  - Historial de activo    → PDF (ficha con timeline)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExportService {

    // ────────────────────────────────────────────────────────────────────────
    // COLORES DE MARCA
    // ────────────────────────────────────────────────────────────────────────

    private static final Color PRIMARY   = new Color(37, 99, 235);   // #2563EB
    private static final Color SECONDARY = new Color(71, 85, 105);   // #475569
    private static final Color LIGHT_BG  = new Color(241, 245, 249); // #F1F5F9
    private static final Color WHITE     = Color.WHITE;
    private static final Color DARK_TEXT = new Color(15, 23, 42);    // #0F172A

    private static final DateTimeFormatter DT_FMT  = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ────────────────────────────────────────────────────────────────────────
    // PDF — REPORTE MENSUAL
    // ────────────────────────────────────────────────────────────────────────

    public byte[] exportMonthlyReportPdf(MonthlyReportDTO report) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 40, 40, 60, 40);
            PdfWriter writer = PdfWriter.getInstance(doc, baos);
            writer.setPageEvent(new PdfHeaderFooter("Reporte Mensual CMMS"));
            doc.open();

            addPdfTitle(doc, "Reporte Mensual de Mantenimiento");
            addPdfSubtitle(doc, "Período: " + report.getMonth().toString());
            doc.add(Chunk.NEWLINE);

            // KPIs en tabla 2x2
            PdfPTable kpiTable = new PdfPTable(2);
            kpiTable.setWidthPercentage(100);
            kpiTable.setSpacingBefore(10f);
            addKpiCell(kpiTable, "Total de Órdenes",   str(report.getTotalWorkOrders()));
            addKpiCell(kpiTable, "Completadas",         str(report.getCompletedWorkOrders()));
            addKpiCell(kpiTable, "Canceladas",          str(report.getCancelledWorkOrders()));
            addKpiCell(kpiTable, "Activos Serviciados", str(report.getAssetsServiced()));
            doc.add(kpiTable);
            doc.add(Chunk.NEWLINE);

            // Tiempos y costos
            addSectionTitle(doc, "Indicadores de Desempeño");
            PdfPTable metricsTable = new PdfPTable(2);
            metricsTable.setWidthPercentage(100);
            metricsTable.setSpacingBefore(8f);
            addMetricRow(metricsTable, "Tiempo promedio de completación", decimal(report.getAvgCompletionTime()) + " hrs");
            addMetricRow(metricsTable, "Tiempo promedio de respuesta",    decimal(report.getAvgResponseTime()) + " hrs");
            addMetricRow(metricsTable, "Costo total de mano de obra",     "$ " + decimal(report.getTotalLaborCosts()));
            addMetricRow(metricsTable, "Costo total",                     "$ " + decimal(report.getTotalCosts()));
            if (report.getMostServicedAssetName() != null) {
                addMetricRow(metricsTable, "Activo más serviciado", report.getMostServicedAssetName());
            }
            doc.add(metricsTable);
            doc.add(Chunk.NEWLINE);

            // OT por prioridad
            if (report.getWorkOrdersByPriority() != null && !report.getWorkOrdersByPriority().isEmpty()) {
                addSectionTitle(doc, "Órdenes por Prioridad");
                PdfPTable priorityTable = buildPdfTable(
                        new String[]{"Prioridad", "Cantidad"},
                        report.getWorkOrdersByPriority().entrySet().stream()
                              .map(e -> new String[]{e.getKey(), e.getValue().toString()})
                              .toList()
                );
                doc.add(priorityTable);
                doc.add(Chunk.NEWLINE);
            }

            // OT por técnico
            if (report.getWorkOrdersByTechnician() != null && !report.getWorkOrdersByTechnician().isEmpty()) {
                addSectionTitle(doc, "Órdenes por Técnico");
                PdfPTable techTable = buildPdfTable(
                        new String[]{"Técnico", "Cantidad"},
                        report.getWorkOrdersByTechnician().entrySet().stream()
                              .map(e -> new String[]{e.getKey(), e.getValue().toString()})
                              .toList()
                );
                doc.add(techTable);
            }

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF reporte mensual", e);
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // EXCEL — REPORTE MENSUAL
    // ────────────────────────────────────────────────────────────────────────

    public byte[] exportMonthlyReportExcel(MonthlyReportDTO report) {
        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // ── Hoja 1: Resumen ──
            Sheet summary = wb.createSheet("Resumen");
            StylePack sp = new StylePack(wb);

            int row = 0;
            setExcelTitle(summary, wb, row++, "Reporte Mensual — " + report.getMonth());
            row++;

            row = writeExcelSection(summary, sp, row, "INDICADORES GENERALES");
            row = writeExcelKv(summary, sp, row, "Total de Órdenes de Trabajo",  str(report.getTotalWorkOrders()));
            row = writeExcelKv(summary, sp, row, "Órdenes Completadas",           str(report.getCompletedWorkOrders()));
            row = writeExcelKv(summary, sp, row, "Órdenes Canceladas",            str(report.getCancelledWorkOrders()));
            row = writeExcelKv(summary, sp, row, "Activos Serviciados",           str(report.getAssetsServiced()));
            row++;

            row = writeExcelSection(summary, sp, row, "TIEMPOS Y COSTOS");
            row = writeExcelKv(summary, sp, row, "Tiempo promedio completación (hrs)", decimal(report.getAvgCompletionTime()));
            row = writeExcelKv(summary, sp, row, "Tiempo promedio respuesta (hrs)",    decimal(report.getAvgResponseTime()));
            row = writeExcelKv(summary, sp, row, "Costo total mano de obra ($)",       decimal(report.getTotalLaborCosts()));
            row = writeExcelKv(summary, sp, row, "Costo total ($)",                    decimal(report.getTotalCosts()));
            if (report.getMostServicedAssetName() != null) {
                row = writeExcelKv(summary, sp, row, "Activo más serviciado", report.getMostServicedAssetName());
            }

            autoSizeColumns(summary, 2);

            // ── Hoja 2: Por Prioridad ──
            if (report.getWorkOrdersByPriority() != null) {
                Sheet prioSheet = wb.createSheet("Por Prioridad");
                writeExcelTable(prioSheet, sp,
                        new String[]{"Prioridad", "Cantidad"},
                        report.getWorkOrdersByPriority().entrySet().stream()
                              .map(e -> new String[]{e.getKey(), e.getValue().toString()})
                              .toList()
                );
                autoSizeColumns(prioSheet, 2);
            }

            // ── Hoja 3: Por Técnico ──
            if (report.getWorkOrdersByTechnician() != null) {
                Sheet techSheet = wb.createSheet("Por Técnico");
                writeExcelTable(techSheet, sp,
                        new String[]{"Técnico", "Órdenes Asignadas"},
                        report.getWorkOrdersByTechnician().entrySet().stream()
                              .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                              .map(e -> new String[]{e.getKey(), e.getValue().toString()})
                              .toList()
                );
                autoSizeColumns(techSheet, 2);
            }

            wb.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando Excel reporte mensual", e);
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // EXCEL — COMPARACIÓN DE TÉCNICOS
    // ────────────────────────────────────────────────────────────────────────

    public byte[] exportTechnicianComparisonExcel(List<TechnicianPerformanceDTO> list) {
        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Performance Técnicos");
            StylePack sp = new StylePack(wb);

            int row = 0;
            setExcelTitle(sheet, wb, row++, "Comparación de Desempeño — Técnicos");
            row++;

            String[] headers = {
                "Técnico", "Total OTs", "Completadas", "Abiertas", "Vencidas",
                "T. Prom. Compl. (hrs)", "Horas Trabajadas", "Tasa Compl. (%)", "A Tiempo (%)", "Revenue ($)"
            };
            writeExcelHeaders(sheet, sp, row++, headers);

            for (TechnicianPerformanceDTO t : list) {
                Row r = sheet.createRow(row++);
                int col = 0;
                createDataCell(r, col++, t.getTechnicianName(),                                  sp.data);
                createDataCell(r, col++, str(t.getTotalWorkOrders()),                            sp.data);
                createDataCell(r, col++, str(t.getCompletedWorkOrders()),                        sp.data);
                createDataCell(r, col++, str(t.getOpenWorkOrders()),                             sp.data);
                createDataCell(r, col++, str(t.getOverdueWorkOrders()),                          sp.data);
                createDataCell(r, col++, decimal(t.getAvgCompletionTime()),                      sp.data);
                createDataCell(r, col++, decimal(t.getTotalHoursWorked()),                       sp.data);
                createDataCell(r, col++, t.getCompletionRate() != null
                        ? String.format("%.1f", t.getCompletionRate()) : "—",                    sp.data);
                createDataCell(r, col++, t.getOnTimeRate() != null
                        ? String.format("%.1f", t.getOnTimeRate()) : "—",                        sp.data);
                createDataCell(r, col,   decimal(t.getTotalRevenue()),                           sp.data);
            }

            autoSizeColumns(sheet, headers.length);
            wb.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando Excel comparación técnicos", e);
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // PDF — HISTORIAL DE ACTIVO
    // ────────────────────────────────────────────────────────────────────────

    public byte[] exportAssetHistoryPdf(Long assetId, String assetName, List<AssetMaintenanceHistoryDTO> history) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate(), 40, 40, 60, 40);  // landscape
            PdfWriter writer = PdfWriter.getInstance(doc, baos);
            writer.setPageEvent(new PdfHeaderFooter("Historial de Mantenimiento"));
            doc.open();

            addPdfTitle(doc, "Historial de Mantenimiento");
            addPdfSubtitle(doc, "Activo: " + (assetName != null ? assetName : "ID " + assetId));
            addPdfSubtitle(doc, "Total de intervenciones: " + history.size());
            doc.add(Chunk.NEWLINE);

            if (history.isEmpty()) {
                Font noDataFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 11, SECONDARY);
                doc.add(new Paragraph("No hay intervenciones registradas para este activo.", noDataFont));
            } else {
                String[] cols = {"Código OT", "Título", "Técnico", "Completada", "Horas", "Costo ($)", "Descripción"};
                float[] widths = {8, 20, 14, 10, 6, 8, 34};

                PdfPTable table = new PdfPTable(cols.length);
                table.setWidthPercentage(100);
                table.setWidths(widths);
                table.setSpacingBefore(8f);

                // Headers
                Font hFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, WHITE);
                for (String col : cols) {
                    PdfPCell cell = new PdfPCell(new Phrase(col, hFont));
                    cell.setBackgroundColor(PRIMARY);
                    cell.setPadding(6);
                    cell.setBorder(Rectangle.NO_BORDER);
                    table.addCell(cell);
                }

                Font dFont = FontFactory.getFont(FontFactory.HELVETICA, 8, DARK_TEXT);
                boolean alt = false;
                for (AssetMaintenanceHistoryDTO h : history) {
                    Color bg = alt ? LIGHT_BG : WHITE;
                    addTableCell(table, dFont, bg, h.getWorkOrderCode());
                    addTableCell(table, dFont, bg, h.getTitle());
                    addTableCell(table, dFont, bg, h.getTechnicianName() != null ? h.getTechnicianName() : "—");
                    addTableCell(table, dFont, bg,
                            h.getCompletedAt() != null ? h.getCompletedAt().format(DT_FMT) : "—");
                    addTableCell(table, dFont, bg, decimal(h.getActualHours()));
                    addTableCell(table, dFont, bg, decimal(h.getCost()));
                    addTableCell(table, dFont, bg,
                            h.getDescription() != null ? truncate(h.getDescription(), 80) : "—");
                    alt = !alt;
                }
                doc.add(table);
            }

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF historial activo", e);
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // HELPERS — PDF
    // ────────────────────────────────────────────────────────────────────────

    private void addPdfTitle(Document doc, String text) throws DocumentException {
        Font f = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, PRIMARY);
        Paragraph p = new Paragraph(text, f);
        p.setAlignment(Element.ALIGN_CENTER);
        doc.add(p);
    }

    private void addPdfSubtitle(Document doc, String text) throws DocumentException {
        Font f = FontFactory.getFont(FontFactory.HELVETICA, 11, SECONDARY);
        Paragraph p = new Paragraph(text, f);
        p.setAlignment(Element.ALIGN_CENTER);
        doc.add(p);
    }

    private void addSectionTitle(Document doc, String text) throws DocumentException {
        Font f = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, PRIMARY);
        Paragraph p = new Paragraph(text, f);
        p.setSpacingBefore(10);
        p.setSpacingAfter(4);
        doc.add(p);
    }

    private void addKpiCell(PdfPTable table, String label, String value) {
        Font lFont = FontFactory.getFont(FontFactory.HELVETICA, 10, SECONDARY);
        Font vFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, DARK_TEXT);
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(LIGHT_BG);
        cell.setPadding(12);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPhrase(new Phrase(label + "\n", lFont));
        cell.addElement(new Phrase(value, vFont));
        table.addCell(cell);
    }

    private void addMetricRow(PdfPTable table, String label, String value) {
        Font lFont = FontFactory.getFont(FontFactory.HELVETICA, 10, DARK_TEXT);
        Font vFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, PRIMARY);
        PdfPCell lCell = new PdfPCell(new Phrase(label, lFont));
        PdfPCell vCell = new PdfPCell(new Phrase(value, vFont));
        lCell.setPadding(6); lCell.setBorder(Rectangle.BOTTOM); lCell.setBorderColor(LIGHT_BG);
        vCell.setPadding(6); vCell.setBorder(Rectangle.BOTTOM); vCell.setBorderColor(LIGHT_BG);
        vCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(lCell);
        table.addCell(vCell);
    }

    private PdfPTable buildPdfTable(String[] headers, List<String[]> rows) throws DocumentException {
        PdfPTable table = new PdfPTable(headers.length);
        table.setWidthPercentage(100);
        table.setSpacingBefore(8f);
        Font hFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, WHITE);
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, hFont));
            cell.setBackgroundColor(PRIMARY);
            cell.setPadding(7);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
        }
        Font dFont = FontFactory.getFont(FontFactory.HELVETICA, 10, DARK_TEXT);
        boolean alt = false;
        for (String[] row : rows) {
            Color bg = alt ? LIGHT_BG : WHITE;
            for (String val : row) addTableCell(table, dFont, bg, val != null ? val : "—");
            alt = !alt;
        }
        return table;
    }

    private void addTableCell(PdfPTable table, Font font, Color bg, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(5);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
    }

    // ────────────────────────────────────────────────────────────────────────
    // HELPERS — EXCEL
    // ────────────────────────────────────────────────────────────────────────

    private void setExcelTitle(Sheet sheet, Workbook wb, int rowIdx, String title) {
        Row row = sheet.createRow(rowIdx);
        Cell cell = row.createCell(0);
        cell.setCellValue(title);
        CellStyle style = wb.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        cell.setCellStyle(style);
    }

    private int writeExcelSection(Sheet sheet, StylePack sp, int rowIdx, String title) {
        Row row = sheet.createRow(rowIdx);
        Cell cell = row.createCell(0);
        cell.setCellValue(title);
        cell.setCellStyle(sp.sectionHeader);
        return rowIdx + 1;
    }

    private int writeExcelKv(Sheet sheet, StylePack sp, int rowIdx, String key, String value) {
        Row row = sheet.createRow(rowIdx);
        Cell k = row.createCell(0);
        Cell v = row.createCell(1);
        k.setCellValue(key);
        v.setCellValue(value);
        k.setCellStyle(sp.label);
        v.setCellStyle(sp.data);
        return rowIdx + 1;
    }

    private void writeExcelHeaders(Sheet sheet, StylePack sp, int rowIdx, String[] headers) {
        Row row = sheet.createRow(rowIdx);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(sp.header);
        }
    }

    private void writeExcelTable(Sheet sheet, StylePack sp, String[] headers, List<String[]> rows) {
        writeExcelHeaders(sheet, sp, 0, headers);
        for (int i = 0; i < rows.size(); i++) {
            Row row = sheet.createRow(i + 1);
            String[] data = rows.get(i);
            for (int j = 0; j < data.length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(data[j] != null ? data[j] : "—");
                cell.setCellStyle(sp.data);
            }
        }
    }

    private void createDataCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "—");
        cell.setCellStyle(style);
    }

    private void autoSizeColumns(Sheet sheet, int count) {
        for (int i = 0; i < count; i++) {
            sheet.autoSizeColumn(i);
            int width = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, Math.min(width + 512, 15000));
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // HELPERS — FORMATO
    // ────────────────────────────────────────────────────────────────────────

    private String str(Number n) {
        return n == null ? "—" : String.valueOf(n);
    }

    private String decimal(BigDecimal n) {
        return n == null ? "—" : String.format("%.2f", n);
    }

    private String truncate(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max - 1) + "…";
    }

    // ────────────────────────────────────────────────────────────────────────
    // ESTILOS EXCEL (inmutables por workbook)
    // ────────────────────────────────────────────────────────────────────────

    private static class StylePack {
        final CellStyle header;
        final CellStyle sectionHeader;
        final CellStyle label;
        final CellStyle data;

        StylePack(Workbook wb) {
            header = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font hf = wb.createFont();
            hf.setBold(true);
            hf.setColor(IndexedColors.WHITE.getIndex());
            header.setFont(hf);
            header.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            header.setAlignment(HorizontalAlignment.CENTER);
            header.setBorderBottom(BorderStyle.THIN);
            header.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());

            sectionHeader = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font shf = wb.createFont();
            shf.setBold(true);
            shf.setColor(IndexedColors.DARK_BLUE.getIndex());
            sectionHeader.setFont(shf);
            sectionHeader.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            sectionHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            label = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font lf = wb.createFont();
            lf.setBold(false);
            label.setFont(lf);

            data = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font df = wb.createFont();
            df.setBold(false);
            data.setFont(df);
            data.setBorderBottom(BorderStyle.THIN);
            data.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // HEADER / FOOTER PDF (número de página)
    // ────────────────────────────────────────────────────────────────────────

    private static class PdfHeaderFooter extends PdfPageEventHelper {
        private final String title;
        private PdfTemplate total;
        private BaseFont bf;

        PdfHeaderFooter(String title) {
            this.title = title;
        }

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            total = writer.getDirectContent().createTemplate(30, 16);
            try {
                bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            cb.saveState();

            // Línea superior
            cb.setColorStroke(new Color(37, 99, 235));
            cb.setLineWidth(2f);
            cb.moveTo(document.left(), document.top() + 15);
            cb.lineTo(document.right(), document.top() + 15);
            cb.stroke();

            // Título en header
            cb.setColorFill(new Color(37, 99, 235));
            cb.beginText();
            cb.setFontAndSize(bf, 9);
            cb.showTextAligned(PdfContentByte.ALIGN_LEFT, title,
                    document.left(), document.top() + 20, 0);
            cb.endText();

            // Footer: página N de total
            cb.setColorFill(new Color(71, 85, 105));
            cb.beginText();
            cb.setFontAndSize(bf, 8);
            float textSize = bf.getWidthPoint("Página " + writer.getPageNumber() + " de ", 8);
            float x = document.right() - textSize - 14;
            cb.showTextAligned(PdfContentByte.ALIGN_LEFT,
                    "Página " + writer.getPageNumber() + " de ", x, document.bottom() - 10, 0);
            cb.endText();

            Image img = Image.getInstance(total);
            img.setAbsolutePosition(document.right() - 14, document.bottom() - 14);
            cb.addImage(img);
            cb.restoreState();
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            total.beginText();
            total.setFontAndSize(bf, 8);
            total.showTextAligned(PdfContentByte.ALIGN_LEFT,
                    String.valueOf(writer.getPageNumber()), 2, 2, 0);
            total.endText();
        }
    }
}
