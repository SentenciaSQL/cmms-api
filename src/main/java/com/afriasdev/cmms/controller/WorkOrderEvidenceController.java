package com.afriasdev.cmms.controller;

import com.afriasdev.cmms.WorkOrderEvidenceDTO;
import com.afriasdev.cmms.dto.request.WorkOrderEvidenceCreateRequest;
import com.afriasdev.cmms.model.EvidenceType;
import com.afriasdev.cmms.service.WorkOrderEvidenceService;
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
@RequestMapping("/api/work-order-evidences")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Work Order Evidences", description = "Gestión de Evidencias de Órdenes de Trabajo")
public class WorkOrderEvidenceController {

    private final WorkOrderEvidenceService evidenceService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    public ResponseEntity<WorkOrderEvidenceDTO> createEvidence(
            @Valid @RequestBody WorkOrderEvidenceCreateRequest request,
            Authentication authentication) {

        Long userId = getUserIdFromAuth(authentication);
        WorkOrderEvidenceDTO created = evidenceService.createEvidence(request, userId);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<WorkOrderEvidenceDTO>> getAllEvidences() {
        List<WorkOrderEvidenceDTO> evidences = evidenceService.getAllEvidences();
        return ResponseEntity.ok(evidences);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    public ResponseEntity<WorkOrderEvidenceDTO> getEvidenceById(@PathVariable Long id) {
        WorkOrderEvidenceDTO evidence = evidenceService.getEvidenceById(id);
        return ResponseEntity.ok(evidence);
    }

    @GetMapping("/work-order/{workOrderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    public ResponseEntity<List<WorkOrderEvidenceDTO>> getEvidencesByWorkOrder(@PathVariable Long workOrderId) {
        List<WorkOrderEvidenceDTO> evidences = evidenceService.getEvidencesByWorkOrder(workOrderId);
        return ResponseEntity.ok(evidences);
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<WorkOrderEvidenceDTO>> getEvidencesByType(@PathVariable EvidenceType type) {
        List<WorkOrderEvidenceDTO> evidences = evidenceService.getEvidencesByType(type);
        return ResponseEntity.ok(evidences);
    }

    @GetMapping("/work-order/{workOrderId}/type/{type}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    public ResponseEntity<List<WorkOrderEvidenceDTO>> getEvidencesByWorkOrderAndType(
            @PathVariable Long workOrderId,
            @PathVariable EvidenceType type) {
        List<WorkOrderEvidenceDTO> evidences = evidenceService.getEvidencesByWorkOrderAndType(workOrderId, type);
        return ResponseEntity.ok(evidences);
    }

    @GetMapping("/work-order/{workOrderId}/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    public ResponseEntity<Long> countEvidencesByWorkOrder(@PathVariable Long workOrderId) {
        Long count = evidenceService.countEvidencesByWorkOrder(workOrderId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/work-order/{workOrderId}/photos/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    public ResponseEntity<Long> countPhotosByWorkOrder(@PathVariable Long workOrderId) {
        Long count = evidenceService.countPhotosByWorkOrder(workOrderId);
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    public ResponseEntity<WorkOrderEvidenceDTO> updateEvidence(
            @PathVariable Long id,
            @Valid @RequestBody WorkOrderEvidenceCreateRequest request) {
        WorkOrderEvidenceDTO updated = evidenceService.updateEvidence(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    public ResponseEntity<Void> deleteEvidence(@PathVariable Long id) {
        evidenceService.deleteEvidence(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/work-order/{workOrderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEvidencesByWorkOrder(@PathVariable Long workOrderId) {
        evidenceService.deleteEvidencesByWorkOrder(workOrderId);
        return ResponseEntity.noContent().build();
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        // TODO: Implementar con tu JWT actual
        return 1L;
    }
}
