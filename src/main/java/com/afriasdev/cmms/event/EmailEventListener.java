package com.afriasdev.cmms.event;

import com.afriasdev.cmms.model.Technician;
import com.afriasdev.cmms.model.WorkOrder;
import com.afriasdev.cmms.security.model.User;
import com.afriasdev.cmms.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Escucha eventos de dominio del CMMS y delega el envío de correos al EmailService.
 *
 * Todos los handlers son @Async — no bloquean el hilo del request.
 * El ApplicationEventPublisher publica de forma síncrona, pero la
 * ejecución real del handler ocurre en el pool de @Async.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailEventListener {

    private final EmailService emailService;

    // ────────────────────────────────────────────────────────────────────────
    // OT ASIGNADA → email al técnico
    // ────────────────────────────────────────────────────────────────────────

    @Async
    @EventListener
    public void onWorkOrderAssigned(WorkOrderAssignedEvent event) {
        WorkOrder wo = event.getWorkOrder();
        Technician tech = wo.getAssignedTech();

        if (tech == null || tech.getUser() == null) {
            log.warn("[EmailListener] WorkOrderAssigned sin técnico asignado (OT {})", wo.getId());
            return;
        }

        User techUser = tech.getUser();
        String email = techUser.getEmail();

        if (email == null || email.isBlank()) {
            log.warn("[EmailListener] Técnico {} sin email, omitiendo notificación", techUser.getUsername());
            return;
        }

        emailService.sendWorkOrderAssigned(
                email,
                techUser.getFirstName() + " " + techUser.getLastName(),
                wo.getCode(),
                wo.getTitle(),
                wo.getPriority() != null ? wo.getPriority().name() : "MEDIUM",
                wo.getDueDate(),
                wo.getId()
        );
    }

    // ────────────────────────────────────────────────────────────────────────
    // CAMBIO DE ESTADO → email al técnico y al creador (si existe)
    // ────────────────────────────────────────────────────────────────────────

    @Async
    @EventListener
    public void onWorkOrderStatusChanged(WorkOrderStatusChangedEvent event) {
        WorkOrder wo = event.getWorkOrder();
        String prev = event.getPreviousStatus().name();
        String next  = event.getNewStatus().name();

        // Notificar al técnico
        Technician tech = wo.getAssignedTech();
        if (tech != null && tech.getUser() != null) {
            User techUser = tech.getUser();
            if (techUser.getEmail() != null && !techUser.getEmail().isBlank()) {
                emailService.sendWorkOrderStatusChanged(
                        techUser.getEmail(),
                        techUser.getFirstName() + " " + techUser.getLastName(),
                        wo.getCode(), wo.getTitle(), prev, next, wo.getId()
                );
            }
        }

        // Notificar al creador (si es distinto del técnico)
        User creator = wo.getCreatedBy();
        if (creator != null && creator.getEmail() != null && !creator.getEmail().isBlank()) {
            boolean isTech = tech != null && tech.getUser() != null
                    && tech.getUser().getId().equals(creator.getId());
            if (!isTech) {
                emailService.sendWorkOrderStatusChanged(
                        creator.getEmail(),
                        creator.getFirstName() + " " + creator.getLastName(),
                        wo.getCode(), wo.getTitle(), prev, next, wo.getId()
                );
            }
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // OT VENCIDA → email al técnico (publicado por el scheduler)
    // ────────────────────────────────────────────────────────────────────────

    @Async
    @EventListener
    public void onWorkOrderOverdue(WorkOrderOverdueEvent event) {
        WorkOrder wo = event.getWorkOrder();
        Technician tech = wo.getAssignedTech();

        if (tech == null || tech.getUser() == null) return;

        User techUser = tech.getUser();
        if (techUser.getEmail() == null || techUser.getEmail().isBlank()) return;

        emailService.sendWorkOrderOverdue(
                techUser.getEmail(),
                techUser.getFirstName() + " " + techUser.getLastName(),
                wo.getCode(),
                wo.getTitle(),
                wo.getDueDate(),
                wo.getId()
        );
    }

    // ────────────────────────────────────────────────────────────────────────
    // USUARIO REGISTRADO → bienvenida
    // ────────────────────────────────────────────────────────────────────────

    @Async
    @EventListener
    public void onUserRegistered(UserRegisteredEvent event) {
        User user = event.getUser();
        if (user.getEmail() == null || user.getEmail().isBlank()) return;

        String role = user.getRoles().stream()
                .findFirst()
                .map(Enum::name)
                .orElse("USER");

        emailService.sendWelcome(
                user.getEmail(),
                user.getFirstName(),
                user.getUsername(),
                role
        );
    }
}
