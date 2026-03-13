package com.afriasdev.cmms.service;

import com.afriasdev.cmms.model.*;
import com.afriasdev.cmms.repository.MaintenancePlanRepository;
import com.afriasdev.cmms.repository.WorkOrderRepository;
import com.afriasdev.cmms.security.model.User;
import com.afriasdev.cmms.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Job que corre periódicamente para generar Órdenes de Trabajo
 * a partir de los Planes de Mantenimiento activos con fecha vencida.
 *
 * Flujo por cada plan elegible:
 *  1. Crear WorkOrder con datos del plan (asset, técnico, prioridad, duración)
 *  2. Actualizar lastExecutionDate = ahora
 *  3. Calcular y guardar el nuevo nextScheduledDate
 *
 * El campo autoGenerateWorkOrder del plan actúa como interruptor:
 * si está en false el plan se omite aunque esté vencido.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceSchedulerService {

    private final MaintenancePlanRepository maintenancePlanRepository;
    private final WorkOrderRepository workOrderRepository;
    private final UserRepository userRepository;

    /**
     * Username del usuario del sistema usado como "creador" de las OTs automáticas.
     * Configurable en application.yaml → app.scheduler.system-username
     * Default: "system" (debe existir en la BD, creado en V2 seed).
     */
    @Value("${app.scheduler.system-username:system}")
    private String systemUsername;

    /**
     * Corre según el cron configurado en application.yaml.
     * Default: cada hora en punto  →  "0 0 * * * *"
     * Para pruebas rápidas usar    →  "0 * * * * *" (cada minuto)
     */
    @Scheduled(cron = "${app.scheduler.maintenance-cron:0 0 * * * *}")
    @Transactional
    public void generateWorkOrdersFromDuePlans() {
        log.info("[Scheduler] Iniciando revisión de planes de mantenimiento vencidos...");

        User systemUser = resolveSystemUser();
        if (systemUser == null) {
            log.error("[Scheduler] Usuario del sistema '{}' no encontrado. Se cancela la ejecución.", systemUsername);
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<MaintenancePlan> duePlans = maintenancePlanRepository.findPlansForAutoWorkOrderGeneration(now);

        if (duePlans.isEmpty()) {
            log.info("[Scheduler] Sin planes vencidos. Nada que generar.");
            return;
        }

        log.info("[Scheduler] {} plan(es) elegibles encontrados.", duePlans.size());

        int generated = 0;
        int skipped = 0;

        for (MaintenancePlan plan : duePlans) {
            try {
                WorkOrder wo = buildWorkOrder(plan, systemUser);
                workOrderRepository.save(wo);

                plan.setLastExecutionDate(now);
                plan.setNextScheduledDate(calculateNextDate(plan, now));
                maintenancePlanRepository.save(plan);

                log.info("[Scheduler] OT '{}' generada para plan '{}' (asset: {}).",
                        wo.getCode(), plan.getName(), plan.getAsset().getName());
                generated++;

            } catch (Exception e) {
                log.error("[Scheduler] Error al procesar plan ID={} '{}': {}",
                        plan.getId(), plan.getName(), e.getMessage(), e);
                skipped++;
            }
        }

        log.info("[Scheduler] Resultado: {} OT(s) generada(s), {} omitida(s).", generated, skipped);
    }

    // -------------------------------------------------------------------------
    // Métodos privados
    // -------------------------------------------------------------------------

    /**
     * Construye una WorkOrder completamente poblada a partir de un MaintenancePlan.
     * Hereda company y site del activo para mantener la trazabilidad.
     */
    private WorkOrder buildWorkOrder(MaintenancePlan plan, User systemUser) {
        WorkOrder wo = new WorkOrder();

        wo.setCode(generateCode());
        wo.setTitle(buildTitle(plan));
        wo.setDescription(plan.getInstructions());
        wo.setStatus(WorkOrderStatus.OPEN);
        wo.setPriority(mapPriority(plan.getPriority()));
        wo.setAsset(plan.getAsset());

        // Heredar site y company del activo
        Asset asset = plan.getAsset();
        if (asset != null && asset.getSite() != null) {
            wo.setSite(asset.getSite());
            if (asset.getSite().getCompany() != null) {
                wo.setCompany(asset.getSite().getCompany());
            }
        }

        // Fechas programadas basadas en nextScheduledDate del plan
        LocalDateTime scheduledStart = plan.getNextScheduledDate() != null
                ? plan.getNextScheduledDate()
                : LocalDateTime.now();

        wo.setScheduledStart(scheduledStart);
        wo.setScheduledEnd(scheduledStart.plusMinutes(plan.getEstimatedDurationMinutes()));
        wo.setDueDate(scheduledStart.toLocalDate());

        // Convertir minutos a horas (escala 2 decimales)
        BigDecimal estimatedHours = BigDecimal
                .valueOf(plan.getEstimatedDurationMinutes())
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        wo.setEstimatedHours(estimatedHours);

        // Técnico asignado en el plan → pre-asignar la OT
        if (plan.getAssignedTechnician() != null) {
            wo.setAssignedTech(plan.getAssignedTechnician());
        }

        wo.setRequester(systemUser);
        wo.setCreatedBy(systemUser);

        return wo;
    }

    /**
     * Calcula el próximo nextScheduledDate según frecuencia y valor del plan.
     */
    private LocalDateTime calculateNextDate(MaintenancePlan plan, LocalDateTime base) {
        int value = plan.getFrequencyValue();
        return switch (plan.getFrequency()) {
            case DAILY     -> base.plusDays(value);
            case WEEKLY    -> base.plusWeeks(value);
            case MONTHLY   -> base.plusMonths(value);
            case QUARTERLY -> base.plusMonths((long) value * 3);
            case YEARLY    -> base.plusYears(value);
        };
    }

    /**
     * Mapea la prioridad del plan (LOW/MEDIUM/HIGH/CRITICAL)
     * a la prioridad de la OT (LOW/MEDIUM/HIGH/URGENT).
     */
    private WorkOrderPriority mapPriority(MaintenancePlan.Priority priority) {
        return switch (priority) {
            case LOW      -> WorkOrderPriority.LOW;
            case MEDIUM   -> WorkOrderPriority.MEDIUM;
            case HIGH     -> WorkOrderPriority.HIGH;
            case CRITICAL -> WorkOrderPriority.URGENT;
        };
    }

    /**
     * Genera un código único para la OT: WO-XXXXX.
     * Usa el total actual de registros para evitar colisiones.
     */
    private String generateCode() {
        long count = workOrderRepository.count();
        return String.format("WO-%05d", count + 1);
    }

    /**
     * Construye el título de la OT incluyendo tipo y nombre del plan.
     * Ejemplo: "[PREVENTIVE] Revisión mensual de compresor #3"
     */
    private String buildTitle(MaintenancePlan plan) {
        return String.format("[%s] %s", plan.getType().name(), plan.getName());
    }

    /**
     * Busca el usuario del sistema por username configurado.
     * Retorna null (con log) si no existe, para que el caller decida cómo manejar el error.
     */
    private User resolveSystemUser() {
        return userRepository.findByUsername(systemUsername).orElse(null);
    }
}
