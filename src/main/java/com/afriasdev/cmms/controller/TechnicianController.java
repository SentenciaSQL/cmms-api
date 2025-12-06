package com.afriasdev.cmms.controller;

import com.afriasdev.cmms.dto.TechnicianDTO;
import com.afriasdev.cmms.service.TechnicianService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/technicians")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Technicians", description = "Gestión de Técnicos ")
public class TechnicianController {

    private final TechnicianService technicianService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TechnicianDTO> createTechnician(@Valid @RequestBody TechnicianDTO technicianDTO) {
        TechnicianDTO created = technicianService.createTechnician(technicianDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<TechnicianDTO>> getAllTechnicians() {
        List<TechnicianDTO> technicians = technicianService.getAllTechnicians();
        return ResponseEntity.ok(technicians);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    public ResponseEntity<TechnicianDTO> getTechnicianById(@PathVariable Long id) {
        TechnicianDTO technician = technicianService.getTechnicianById(id);
        return ResponseEntity.ok(technician);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    public ResponseEntity<TechnicianDTO> getTechnicianByUserId(@PathVariable Long userId) {
        TechnicianDTO technician = technicianService.getTechnicianByUserId(userId);
        return ResponseEntity.ok(technician);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<TechnicianDTO>> getActiveTechnicians() {
        List<TechnicianDTO> technicians = technicianService.getActiveTechnicians();
        return ResponseEntity.ok(technicians);
    }

    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<TechnicianDTO>> getAvailableTechnicians(
            @RequestParam(defaultValue = "5") int maxWorkOrders) {
        List<TechnicianDTO> technicians = technicianService.getAvailableTechnicians(maxWorkOrders);
        return ResponseEntity.ok(technicians);
    }

    @GetMapping("/skill/{skillLevel}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<TechnicianDTO>> getTechniciansBySkillLevel(@PathVariable String skillLevel) {
        List<TechnicianDTO> technicians = technicianService.getTechniciansBySkillLevel(skillLevel);
        return ResponseEntity.ok(technicians);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TechnicianDTO> updateTechnician(
            @PathVariable Long id,
            @Valid @RequestBody TechnicianDTO technicianDTO) {
        TechnicianDTO updated = technicianService.updateTechnician(id, technicianDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTechnician(@PathVariable Long id) {
        technicianService.deleteTechnician(id);
        return ResponseEntity.noContent().build();
    }
}
