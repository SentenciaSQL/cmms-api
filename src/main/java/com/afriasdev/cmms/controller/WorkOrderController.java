package com.afriasdev.cmms.controller;

import com.afriasdev.cmms.dto.WorkOrderDTO;
import com.afriasdev.cmms.dto.request.WorkOrderCreateRequest;
import com.afriasdev.cmms.dto.request.WorkOrderFilterRequest;
import com.afriasdev.cmms.dto.request.WorkOrderUpdateStatusRequest;
import com.afriasdev.cmms.dto.response.PaginatedResponse;
import com.afriasdev.cmms.model.WorkOrderStatus;
import com.afriasdev.cmms.service.WorkOrderService;
import com.afriasdev.cmms.security.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/work-orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Work Orders", description = "Gestión de Órdenes de Trabajo - Núcleo del CMMS")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    /**
     * Listar con paginación
     */
    @GetMapping("/paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Listar órdenes con paginación y ordenamiento")
    public ResponseEntity<PaginatedResponse<WorkOrderDTO>> getAllWorkOrdersPaginated(
            @Parameter(description = "Número de página (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo para ordenar") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Dirección de ordenamiento (ASC/DESC)") @RequestParam(defaultValue = "DESC") String sortDirection) {

        PaginatedResponse<WorkOrderDTO> response = workOrderService.getAllWorkOrdersPaginated(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }

    /**
     * Filtrar con paginación
     */
    @PostMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    @Operation(
            summary = "Filtrar órdenes con múltiples criterios",
            description = """
            Filtros disponibles:
            - statuses: Lista de estados (OPEN, IN_PROGRESS, COMPLETED, CANCELLED)
            - priorities: Lista de prioridades (LOW, MEDIUM, HIGH, URGENT)
            - companyId, siteId, assetId, assignedTechId, requesterId
            - dueDateFrom, dueDateTo: Rango de fecha límite
            - createdFrom, createdTo: Rango de fecha de creación
            - overdue: true para ver solo vencidas
            - unassigned: true para ver solo sin asignar
            - search: Buscar en título, descripción o código
            - page, size, sortBy, sortDirection: Paginación y ordenamiento
            """
    )
    public ResponseEntity<PaginatedResponse<WorkOrderDTO>> filterWorkOrders(
            @RequestBody WorkOrderFilterRequest filter) {

        PaginatedResponse<WorkOrderDTO> response = workOrderService.filterWorkOrders(filter);
        return ResponseEntity.ok(response);
    }

    /**
     * Buscar con paginación
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Buscar órdenes en título, descripción o código")
    public ResponseEntity<PaginatedResponse<WorkOrderDTO>> searchWorkOrders(
            @Parameter(description = "Texto a buscar") @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PaginatedResponse<WorkOrderDTO> response = workOrderService.searchWorkOrders(query, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Por estado con paginación
     */
    @GetMapping("/status/{status}/paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Obtener órdenes por estado con paginación")
    public ResponseEntity<PaginatedResponse<WorkOrderDTO>> getWorkOrdersByStatusPaginated(
            @PathVariable WorkOrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PaginatedResponse<WorkOrderDTO> response = workOrderService.getWorkOrdersByStatusPaginated(status, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Por técnico con paginación
     */
    @GetMapping("/technician/{techId}/paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Obtener órdenes por técnico con paginación")
    public ResponseEntity<PaginatedResponse<WorkOrderDTO>> getWorkOrdersByTechnicianPaginated(
            @PathVariable Long techId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PaginatedResponse<WorkOrderDTO> response = workOrderService.getWorkOrdersByTechnicianPaginated(techId, page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'REQUESTER')")
    @Operation(
            summary = "Crear nueva orden de trabajo",
            description = "Crea una nueva orden de trabajo. El código se genera automáticamente (WO-00001, WO-00002, etc.)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Orden de trabajo creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autorizado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content)
    })
    public ResponseEntity<WorkOrderDTO> createWorkOrder(
            @Valid @RequestBody WorkOrderCreateRequest request,
            Authentication authentication) {

        Long userId = getUserIdFromAuth(authentication);
        WorkOrderDTO created = workOrderService.createWorkOrder(request, userId);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    @Operation(
            summary = "Listar todas las órdenes de trabajo",
            description = "Obtiene una lista completa de todas las órdenes de trabajo en el sistema"
    )
    @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    public ResponseEntity<List<WorkOrderDTO>> getAllWorkOrders() {
        List<WorkOrderDTO> workOrders = workOrderService.getAllWorkOrders();
        return ResponseEntity.ok(workOrders);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN', 'REQUESTER')")
    @Operation(
            summary = "Obtener orden de trabajo por ID",
            description = "Obtiene los detalles completos de una orden de trabajo específica"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orden de trabajo encontrada"),
            @ApiResponse(responseCode = "404", description = "Orden de trabajo no encontrada", content = @Content)
    })
    public ResponseEntity<WorkOrderDTO> getWorkOrderById(@PathVariable Long id) {
        WorkOrderDTO workOrder = workOrderService.getWorkOrderById(id);
        return ResponseEntity.ok(workOrder);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    @Operation(
            summary = "Filtrar órdenes por estado",
            description = "Obtiene todas las órdenes de trabajo con un estado específico (OPEN, IN_PROGRESS, COMPLETED, CANCELLED)"
    )
    public ResponseEntity<List<WorkOrderDTO>> getWorkOrdersByStatus(@PathVariable WorkOrderStatus status) {
        List<WorkOrderDTO> workOrders = workOrderService.getWorkOrdersByStatus(status);
        return ResponseEntity.ok(workOrders);
    }

    @GetMapping("/technician/{techId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    public ResponseEntity<List<WorkOrderDTO>> getWorkOrdersByTechnician(@PathVariable Long techId) {
        List<WorkOrderDTO> workOrders = workOrderService.getWorkOrdersByTechnician(techId);
        return ResponseEntity.ok(workOrders);
    }

    @GetMapping("/asset/{assetId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<WorkOrderDTO>> getWorkOrdersByAsset(@PathVariable Long assetId) {
        List<WorkOrderDTO> workOrders = workOrderService.getWorkOrdersByAsset(assetId);
        return ResponseEntity.ok(workOrders);
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "Obtener órdenes vencidas",
            description = "Lista todas las órdenes de trabajo que han superado su fecha límite y aún no están completadas"
    )
    public ResponseEntity<List<WorkOrderDTO>> getOverdueWorkOrders() {
        List<WorkOrderDTO> workOrders = workOrderService.getOverdueWorkOrders();
        return ResponseEntity.ok(workOrders);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<WorkOrderDTO> updateWorkOrder(
            @PathVariable Long id,
            @Valid @RequestBody WorkOrderCreateRequest request,
            Authentication authentication) {

        Long userId = getUserIdFromAuth(authentication);
        WorkOrderDTO updated = workOrderService.updateWorkOrder(id, request, userId);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/assign/{technicianId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "Asignar técnico a orden de trabajo",
            description = "Asigna un técnico específico a una orden de trabajo"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Técnico asignado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Orden o técnico no encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "El técnico no está activo", content = @Content)
    })
    public ResponseEntity<WorkOrderDTO> assignTechnician(
            @PathVariable Long id,
            @PathVariable Long technicianId,
            Authentication authentication) {

        Long userId = getUserIdFromAuth(authentication);
        WorkOrderDTO updated = workOrderService.assignTechnician(id, technicianId, userId);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    @Operation(
            summary = "Actualizar estado de orden de trabajo",
            description = """
            Cambia el estado de una orden de trabajo. Transiciones válidas:
            - OPEN → IN_PROGRESS o CANCELLED
            - IN_PROGRESS → COMPLETED o CANCELLED
            - COMPLETED y CANCELLED son estados finales
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Transición de estado inválida", content = @Content),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada", content = @Content)
    })
    public ResponseEntity<WorkOrderDTO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody WorkOrderUpdateStatusRequest request,
            Authentication authentication) {

        Long userId = getUserIdFromAuth(authentication);
        WorkOrderDTO updated = workOrderService.updateStatus(id, request, userId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "Cancelar orden de trabajo",
            description = "Marca una orden de trabajo como CANCELLED (soft delete)"
    )
    @ApiResponse(responseCode = "204", description = "Orden cancelada exitosamente")
    public ResponseEntity<Void> deleteWorkOrder(@PathVariable Long id) {
        workOrderService.deleteWorkOrder(id);
        return ResponseEntity.noContent().build();
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        return SecurityUtils.getUserIdFromAuth(authentication);
    }
}
