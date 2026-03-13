package com.afriasdev.cmms.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Servicio de envío de correos electrónicos del CMMS.
 *
 * Todos los métodos son @Async — los envíos no bloquean el hilo del request.
 * Si app.email.enabled=false (útil en dev), los correos se loguean pero no se envían.
 *
 * Templates disponibles:
 *  - workOrderAssigned        → técnico asignado
 *  - workOrderStatusChanged   → cambio de estado (completada, cancelada, en progreso)
 *  - workOrderOverdue         → OT vencida, alerta al técnico
 *  - welcome                  → bienvenida a usuario nuevo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String from;

    @Value("${app.email.from-name}")
    private String fromName;

    @Value("${app.email.base-url}")
    private String baseUrl;

    @Value("${app.email.enabled:true}")
    private boolean enabled;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ────────────────────────────────────────────────────────────────────────
    // EMAILS DE NEGOCIO
    // ────────────────────────────────────────────────────────────────────────

    @Async
    public void sendWorkOrderAssigned(
            String technicianEmail,
            String technicianName,
            String workOrderCode,
            String workOrderTitle,
            String priority,
            LocalDate dueDate,
            Long workOrderId) {

        String dueDateStr = dueDate != null ? dueDate.format(DATE_FMT) : "Sin fecha límite";
        String link = baseUrl + "/work-orders/" + workOrderId;

        String subject = "[CMMS] Nueva OT asignada: " + workOrderCode;

        String body = buildEmail(
                "Nueva Orden de Trabajo Asignada",
                "Hola " + technicianName + ",",
                "Se te ha asignado una nueva orden de trabajo que requiere tu atención.",
                new String[][]{
                    {"Código",      workOrderCode},
                    {"Título",      workOrderTitle},
                    {"Prioridad",   priorityBadge(priority)},
                    {"Fecha límite", dueDateStr}
                },
                "Ver Orden de Trabajo",
                link,
                "#2563EB"
        );

        send(technicianEmail, subject, body);
    }

    @Async
    public void sendWorkOrderStatusChanged(
            String recipientEmail,
            String recipientName,
            String workOrderCode,
            String workOrderTitle,
            String previousStatus,
            String newStatus,
            Long workOrderId) {

        String link = baseUrl + "/work-orders/" + workOrderId;
        String color = statusColor(newStatus);
        String subject = "[CMMS] OT " + workOrderCode + " — Estado: " + translateStatus(newStatus);

        String body = buildEmail(
                "Actualización de Orden de Trabajo",
                "Hola " + recipientName + ",",
                "El estado de la siguiente orden de trabajo ha sido actualizado.",
                new String[][]{
                    {"Código",          workOrderCode},
                    {"Título",          workOrderTitle},
                    {"Estado anterior", translateStatus(previousStatus)},
                    {"Nuevo estado",    statusBadge(newStatus)}
                },
                "Ver Orden de Trabajo",
                link,
                color
        );

        send(recipientEmail, subject, body);
    }

    @Async
    public void sendWorkOrderOverdue(
            String recipientEmail,
            String recipientName,
            String workOrderCode,
            String workOrderTitle,
            LocalDate dueDate,
            Long workOrderId) {

        String dueDateStr = dueDate != null ? dueDate.format(DATE_FMT) : "—";
        String link = baseUrl + "/work-orders/" + workOrderId;
        String subject = "[CMMS] ⚠ OT Vencida: " + workOrderCode;

        String body = buildEmail(
                "Orden de Trabajo Vencida",
                "Hola " + recipientName + ",",
                "La siguiente orden de trabajo está vencida y requiere acción inmediata.",
                new String[][]{
                    {"Código",      workOrderCode},
                    {"Título",      workOrderTitle},
                    {"Venció el",   dueDateStr}
                },
                "Atender Ahora",
                link,
                "#DC2626"
        );

        send(recipientEmail, subject, body);
    }

    @Async
    public void sendWelcome(
            String userEmail,
            String firstName,
            String username,
            String role) {

        String link = baseUrl + "/login";
        String subject = "Bienvenido al sistema CMMS";

        String body = buildEmail(
                "¡Bienvenido al CMMS!",
                "Hola " + firstName + ",",
                "Tu cuenta ha sido creada exitosamente. Ya puedes acceder al sistema de gestión de mantenimiento.",
                new String[][]{
                    {"Usuario", username},
                    {"Rol",     translateRole(role)}
                },
                "Iniciar Sesión",
                link,
                "#2563EB"
        );

        send(userEmail, subject, body);
    }

    // ────────────────────────────────────────────────────────────────────────
    // ENVÍO REAL
    // ────────────────────────────────────────────────────────────────────────

    private void send(String to, String subject, String htmlBody) {
        if (!enabled) {
            log.info("[Email DISABLED] Para: {} | Asunto: {}", to, subject);
            return;
        }
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    msg, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(msg);
            log.info("[Email] Enviado a {} | {}", to, subject);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("[Email] Error enviando a {}: {}", to, e.getMessage());
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // TEMPLATE HTML
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Genera el HTML completo del correo.
     *
     * @param heading        Título principal del correo
     * @param greeting       Saludo personalizado
     * @param intro          Párrafo introductorio
     * @param tableRows      Filas de la tabla de datos: [[label, value], ...]
     * @param ctaLabel       Texto del botón CTA
     * @param ctaUrl         URL del botón
     * @param accentColor    Color hexadecimal del acento (#2563EB, #DC2626, etc.)
     */
    private String buildEmail(
            String heading,
            String greeting,
            String intro,
            String[][] tableRows,
            String ctaLabel,
            String ctaUrl,
            String accentColor) {

        StringBuilder rows = new StringBuilder();
        for (String[] row : tableRows) {
            rows.append("""
                    <tr>
                      <td style="padding:10px 16px;color:#64748B;font-size:13px;width:140px;
                                 border-bottom:1px solid #F1F5F9;white-space:nowrap">%s</td>
                      <td style="padding:10px 16px;color:#0F172A;font-size:13px;font-weight:600;
                                 border-bottom:1px solid #F1F5F9">%s</td>
                    </tr>
                    """.formatted(row[0], row[1]));
        }

        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width,initial-scale=1">
                  <title>%s</title>
                </head>
                <body style="margin:0;padding:0;background:#F8FAFC;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif">
                  <table width="100%%" cellpadding="0" cellspacing="0">
                    <tr><td align="center" style="padding:40px 20px">
                      <table width="600" cellpadding="0" cellspacing="0"
                             style="background:#FFFFFF;border-radius:12px;overflow:hidden;
                                    box-shadow:0 4px 24px rgba(0,0,0,.08)">

                        <!-- Header -->
                        <tr><td style="background:%s;padding:28px 32px">
                          <h1 style="margin:0;color:#FFFFFF;font-size:22px;font-weight:700;
                                     letter-spacing:-0.3px">⚙ CMMS Manager</h1>
                          <p style="margin:4px 0 0;color:rgba(255,255,255,.75);font-size:13px">
                            Sistema de Gestión de Mantenimiento</p>
                        </td></tr>

                        <!-- Body -->
                        <tr><td style="padding:32px">
                          <h2 style="margin:0 0 8px;color:#0F172A;font-size:20px;font-weight:700">%s</h2>
                          <p style="margin:0 0 4px;color:#475569;font-size:15px">%s</p>
                          <p style="margin:0 0 24px;color:#64748B;font-size:14px;line-height:1.6">%s</p>

                          <!-- Data table -->
                          <table width="100%%" cellpadding="0" cellspacing="0"
                                 style="background:#F8FAFC;border-radius:8px;overflow:hidden;
                                        border:1px solid #E2E8F0">
                            %s
                          </table>

                          <!-- CTA -->
                          <div style="text-align:center;margin:32px 0 8px">
                            <a href="%s"
                               style="display:inline-block;padding:14px 32px;background:%s;
                                      color:#FFFFFF;text-decoration:none;border-radius:8px;
                                      font-size:15px;font-weight:600;letter-spacing:0.2px">
                              %s →
                            </a>
                          </div>
                        </td></tr>

                        <!-- Footer -->
                        <tr><td style="background:#F1F5F9;padding:20px 32px;border-top:1px solid #E2E8F0">
                          <p style="margin:0;color:#94A3B8;font-size:12px;text-align:center;line-height:1.6">
                            Este correo fue generado automáticamente por CMMS Manager.<br>
                            Por favor no respondas a este mensaje.
                          </p>
                        </td></tr>

                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(
                heading,
                accentColor,
                heading,
                greeting,
                intro,
                rows,
                ctaUrl,
                accentColor,
                ctaLabel
        );
    }

    // ────────────────────────────────────────────────────────────────────────
    // HELPERS DE FORMATO
    // ────────────────────────────────────────────────────────────────────────

    private String priorityBadge(String priority) {
        String color = switch (priority.toUpperCase()) {
            case "URGENT"   -> "#DC2626";
            case "HIGH"     -> "#EA580C";
            case "MEDIUM"   -> "#D97706";
            default         -> "#64748B";
        };
        return badge(priority, color);
    }

    private String statusBadge(String status) {
        return badge(translateStatus(status), statusColor(status));
    }

    private String badge(String text, String color) {
        return "<span style=\"display:inline-block;padding:2px 10px;background:" + color +
               ";color:#fff;border-radius:20px;font-size:12px;font-weight:600\">" + text + "</span>";
    }

    private String statusColor(String status) {
        return switch (status.toUpperCase()) {
            case "COMPLETED"    -> "#16A34A";
            case "CANCELLED"    -> "#DC2626";
            case "IN_PROGRESS"  -> "#2563EB";
            default             -> "#64748B";
        };
    }

    private String translateStatus(String status) {
        return switch (status.toUpperCase()) {
            case "OPEN"         -> "Abierta";
            case "IN_PROGRESS"  -> "En Progreso";
            case "COMPLETED"    -> "Completada";
            case "CANCELLED"    -> "Cancelada";
            default             -> status;
        };
    }

    private String translateRole(String role) {
        String r = role.replace("ROLE_", "");
        return switch (r.toUpperCase()) {
            case "ADMIN"        -> "Administrador";
            case "MANAGER"      -> "Gerente";
            case "TECHNICIAN"   -> "Técnico";
            case "REQUESTER"    -> "Solicitante";
            default             -> r;
        };
    }
}
