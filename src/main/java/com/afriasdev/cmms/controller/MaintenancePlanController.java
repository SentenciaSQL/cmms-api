package com.afriasdev.cmms.controller;

import com.afriasdev.cmms.dto.MaintenancePlanDTO;
import com.afriasdev.cmms.dto.request.MaintenancePlanCreateRequest;
import com.afriasdev.cmms.dto.request.MaintenancePlanUpdateRequest;
import com.afriasdev.cmms.service.MaintenancePlanService;
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
@RequestMapping("/api/maintenance-plans")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Maintenance Plans", description = "API para gestión de planes de mantenimiento preventivo")
public class MaintenancePlanController {

    private final MaintenancePlanService maintenancePlanService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Crear plan de mantenimiento", description = "Crea un nuevo plan de mantenimiento preventivo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Plan creado exitosamente",
                    content = @Content(schema = @Schema(implementation = MaintenancePlanDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    public ResponseEntity<MaintenancePlanDTO> create(
            @Valid @RequestBody MaintenancePlanCreateRequest request) {
        MaintenancePlanDTO created = maintenancePlanService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Listar todos los planes", description = "Obtiene todos los planes de mantenimiento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    public ResponseEntity<List<MaintenancePlanDTO>> findAll() {
        List<MaintenancePlanDTO> plans = maintenancePlanService.findAll();
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Obtener plan por ID", description = "Obtiene un plan de mantenimiento específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plan encontrado"),
            @ApiResponse(responseCode = "404", description = "Plan no encontrado"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    public ResponseEntity<MaintenancePlanDTO> findById(
            @Parameter(description = "ID del plan de mantenimiento") @PathVariable Long id) {
        MaintenancePlanDTO plan = maintenancePlanService.findById(id);
        return ResponseEntity.ok(plan);
    }

    @GetMapping("/asset/{assetId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Obtener planes por activo", description = "Obtiene todos los planes de un activo específico")
    public ResponseEntity<List<MaintenancePlanDTO>> findByAsset(
            @Parameter(description = "ID del activo") @PathVariable Long assetId) {
        List<MaintenancePlanDTO> plans = maintenancePlanService.findByAssetId(assetId);
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/due")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Obtener planes vencidos", description = "Obtiene todos los planes de mantenimiento que están vencidos")
    public ResponseEntity<List<MaintenancePlanDTO>> findDuePlans() {
        List<MaintenancePlanDTO> plans = maintenancePlanService.findDuePlans();
        return ResponseEntity.ok(plans);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Actualizar plan", description = "Actualiza un plan de mantenimiento existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plan actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Plan no encontrado"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    public ResponseEntity<MaintenancePlanDTO> update(
            @Parameter(description = "ID del plan") @PathVariable Long id,
            @Valid @RequestBody MaintenancePlanUpdateRequest request) {
        MaintenancePlanDTO updated = maintenancePlanService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar plan", description = "Elimina (desactiva) un plan de mantenimiento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Plan eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Plan no encontrado"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID del plan") @PathVariable Long id) {
        maintenancePlanService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/execute")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Ejecutar plan", description = "Genera una orden de trabajo a partir del plan de mantenimiento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orden de trabajo generada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Plan no encontrado"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    public ResponseEntity<Void> execute(
            @Parameter(description = "ID del plan") @PathVariable Long id) {
        maintenancePlanService.executeMaintenancePlan(id);
        return ResponseEntity.ok().build();
    }
}