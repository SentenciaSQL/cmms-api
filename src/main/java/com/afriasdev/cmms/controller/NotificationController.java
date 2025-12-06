package com.afriasdev.cmms.controller;

import com.afriasdev.cmms.dto.NotificationDTO;
import com.afriasdev.cmms.dto.request.NotificationCreateRequest;
import com.afriasdev.cmms.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Notifications", description = "API para gestión de notificaciones del sistema")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear notificación", description = "Crea una nueva notificación para un usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Notificación creada exitosamente",
                    content = @Content(schema = @Schema(implementation = NotificationDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    public ResponseEntity<NotificationDTO> create(
            @Valid @RequestBody NotificationCreateRequest request) {
        NotificationDTO created = notificationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN', 'CUSTOMER')")
    @Operation(summary = "Obtener notificaciones de usuario",
            description = "Obtiene todas las notificaciones de un usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    public ResponseEntity<List<NotificationDTO>> findByUser(
            @Parameter(description = "ID del usuario") @PathVariable Long userId) {
        List<NotificationDTO> notifications = notificationService.findByUserId(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}/unread")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN', 'CUSTOMER')")
    @Operation(summary = "Obtener notificaciones no leídas",
            description = "Obtiene todas las notificaciones no leídas de un usuario")
    public ResponseEntity<List<NotificationDTO>> findUnread(
            @Parameter(description = "ID del usuario") @PathVariable Long userId) {
        List<NotificationDTO> notifications = notificationService.findUnreadByUserId(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}/count-unread")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN', 'CUSTOMER')")
    @Operation(summary = "Contar notificaciones no leídas",
            description = "Obtiene el número de notificaciones no leídas de un usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conteo obtenido exitosamente"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    public ResponseEntity<Long> countUnread(
            @Parameter(description = "ID del usuario") @PathVariable Long userId) {
        Long count = notificationService.countUnread(userId);
        return ResponseEntity.ok(count);
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN', 'CUSTOMER')")
    @Operation(summary = "Marcar notificación como leída",
            description = "Marca una notificación específica como leída")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificación marcada como leída"),
            @ApiResponse(responseCode = "404", description = "Notificación no encontrada"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    public ResponseEntity<NotificationDTO> markAsRead(
            @Parameter(description = "ID de la notificación") @PathVariable Long id) {
        NotificationDTO notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(notification);
    }

    @PatchMapping("/user/{userId}/mark-all-read")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN', 'CUSTOMER')")
    @Operation(summary = "Marcar todas como leídas",
            description = "Marca todas las notificaciones de un usuario como leídas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificaciones marcadas exitosamente"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    public ResponseEntity<Void> markAllAsRead(
            @Parameter(description = "ID del usuario") @PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN', 'CUSTOMER')")
    @Operation(summary = "Eliminar notificación", description = "Elimina una notificación específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Notificación eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Notificación no encontrada"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID de la notificación") @PathVariable Long id) {
        notificationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user/{userId}/read")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN', 'CUSTOMER')")
    @Operation(summary = "Eliminar notificaciones leídas",
            description = "Elimina todas las notificaciones leídas de un usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Notificaciones eliminadas exitosamente"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    public ResponseEntity<Void> deleteReadNotifications(
            @Parameter(description = "ID del usuario") @PathVariable Long userId) {
        notificationService.deleteReadNotifications(userId);
        return ResponseEntity.noContent().build();
    }
}
