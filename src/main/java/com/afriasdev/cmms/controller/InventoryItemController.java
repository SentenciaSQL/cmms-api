package com.afriasdev.cmms.controller;

import com.afriasdev.cmms.dto.InventoryItemDTO;
import com.afriasdev.cmms.dto.request.InventoryItemCreateRequest;
import com.afriasdev.cmms.dto.request.StockAdjustmentRequest;
import com.afriasdev.cmms.service.InventoryItemService;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory-items")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Inventory Items", description = "API para gestión de ítems de inventario (repuestos, herramientas, consumibles)")
public class InventoryItemController {

    private final InventoryItemService inventoryItemService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Crear ítem de inventario", description = "Crea un nuevo ítem en el inventario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ítem creado exitosamente",
                    content = @Content(schema = @Schema(implementation = InventoryItemDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    public ResponseEntity<InventoryItemDTO> create(
            @Valid @RequestBody InventoryItemCreateRequest request) {
        InventoryItemDTO created = inventoryItemService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Listar todos los ítems", description = "Obtiene todos los ítems del inventario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    public ResponseEntity<List<InventoryItemDTO>> findAll() {
        List<InventoryItemDTO> items = inventoryItemService.findAll();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Obtener ítem por ID", description = "Obtiene un ítem específico del inventario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ítem encontrado"),
            @ApiResponse(responseCode = "404", description = "Ítem no encontrado"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    public ResponseEntity<InventoryItemDTO> findById(
            @Parameter(description = "ID del ítem") @PathVariable Long id) {
        InventoryItemDTO item = inventoryItemService.findById(id);
        return ResponseEntity.ok(item);
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Obtener ítems con stock bajo",
            description = "Obtiene todos los ítems cuyo stock actual está por debajo del mínimo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    public ResponseEntity<List<InventoryItemDTO>> findLowStock() {
        List<InventoryItemDTO> items = inventoryItemService.findLowStock();
        return ResponseEntity.ok(items);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Actualizar ítem", description = "Actualiza un ítem del inventario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ítem actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Ítem no encontrado"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    public ResponseEntity<InventoryItemDTO> update(
            @Parameter(description = "ID del ítem") @PathVariable Long id,
            @Valid @RequestBody InventoryItemCreateRequest request) {
        InventoryItemDTO updated = inventoryItemService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Ajustar stock",
            description = "Ajusta el stock de un ítem (positivo para añadir, negativo para reducir)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock ajustado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Stock insuficiente o datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Ítem no encontrado"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    public ResponseEntity<InventoryItemDTO> adjustStock(
            @Parameter(description = "ID del ítem") @PathVariable Long id,
            @Valid @RequestBody StockAdjustmentRequest request,
            Authentication authentication) {
        // Extraer userId del authentication - AJUSTA según tu implementación
        Long userId = extractUserIdFromAuthentication(authentication);
        InventoryItemDTO updated = inventoryItemService.adjustStock(id, request, userId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar ítem", description = "Elimina (desactiva) un ítem del inventario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Ítem eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Ítem no encontrado"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID del ítem") @PathVariable Long id) {
        inventoryItemService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Helper method - AJUSTA según tu implementación de autenticación
    private Long extractUserIdFromAuthentication(Authentication authentication) {
        // Opción 1: Si usas UserDetails custom
        // UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        // return userDetails.getId();

        // Opción 2: Placeholder - implementa según tu lógica
        return 1L;
    }
}
