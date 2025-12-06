package com.afriasdev.cmms.controller;

import com.afriasdev.cmms.dto.InventoryMovementDTO;
import com.afriasdev.cmms.dto.request.InventoryMovementCreateRequest;
import com.afriasdev.cmms.service.InventoryMovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/inventory-movements")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Inventory Movements", description = "API para gestión de movimientos de inventario (entradas, salidas, ajustes)")
public class InventoryMovementController {

    private final InventoryMovementService movementService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Registrar movimiento", description = "Registra un movimiento de inventario (entrada, salida o ajuste)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Movimiento registrado exitosamente",
                    content = @Content(schema = @Schema(implementation = InventoryMovementDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o stock insuficiente"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    public ResponseEntity<InventoryMovementDTO> create(
            @Valid @RequestBody InventoryMovementCreateRequest request) {
        InventoryMovementDTO created = movementService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Listar todos los movimientos", description = "Obtiene todos los movimientos de inventario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    public ResponseEntity<List<InventoryMovementDTO>> findAll() {
        List<InventoryMovementDTO> movements = movementService.findAll();
        return ResponseEntity.ok(movements);
    }

    @GetMapping("/item/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Obtener movimientos por ítem", description = "Obtiene todos los movimientos de un ítem específico")
    public ResponseEntity<List<InventoryMovementDTO>> findByItem(
            @Parameter(description = "ID del ítem de inventario") @PathVariable Long itemId) {
        List<InventoryMovementDTO> movements = movementService.findByInventoryItemId(itemId);
        return ResponseEntity.ok(movements);
    }

    @GetMapping("/work-order/{workOrderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Obtener movimientos por orden de trabajo",
            description = "Obtiene todos los movimientos asociados a una orden de trabajo")
    public ResponseEntity<List<InventoryMovementDTO>> findByWorkOrder(
            @Parameter(description = "ID de la orden de trabajo") @PathVariable Long workOrderId) {
        List<InventoryMovementDTO> movements = movementService.findByWorkOrderId(workOrderId);
        return ResponseEntity.ok(movements);
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Obtener movimientos por rango de fechas",
            description = "Obtiene todos los movimientos dentro de un rango de fechas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
            @ApiResponse(responseCode = "400", description = "Rango de fechas inválido"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    public ResponseEntity<List<InventoryMovementDTO>> findByDateRange(
            @Parameter(description = "Fecha de inicio (formato: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Fecha de fin (formato: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<InventoryMovementDTO> movements = movementService.findByDateRange(startDate, endDate);
        return ResponseEntity.ok(movements);
    }
}
