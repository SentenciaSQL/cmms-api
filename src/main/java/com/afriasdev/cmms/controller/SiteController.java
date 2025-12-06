package com.afriasdev.cmms.controller;

import com.afriasdev.cmms.dto.SiteDTO;
import com.afriasdev.cmms.dto.request.SiteCreateRequest;
import com.afriasdev.cmms.service.SiteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sites")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Sites", description = "Gestión de Sitios")
public class SiteController {
    private final SiteService siteService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<SiteDTO> createSite(@Valid @RequestBody SiteCreateRequest request) {
        SiteDTO created = siteService.createSite(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    public ResponseEntity<List<SiteDTO>> getAllSites() {
        List<SiteDTO> sites = siteService.getAllSites();
        return ResponseEntity.ok(sites);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    public ResponseEntity<SiteDTO> getSiteById(@PathVariable Long id) {
        SiteDTO site = siteService.getSiteById(id);
        return ResponseEntity.ok(site);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    public ResponseEntity<List<SiteDTO>> getActiveSites() {
        List<SiteDTO> sites = siteService.getActiveSites();
        return ResponseEntity.ok(sites);
    }

    @GetMapping("/company/{companyId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<SiteDTO>> getSitesByCompany(@PathVariable Long companyId) {
        List<SiteDTO> sites = siteService.getSitesByCompany(companyId);
        return ResponseEntity.ok(sites);
    }

    @GetMapping("/company/{companyId}/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<SiteDTO>> getActiveSitesByCompany(@PathVariable Long companyId) {
        List<SiteDTO> sites = siteService.getActiveSitesByCompany(companyId);
        return ResponseEntity.ok(sites);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<SiteDTO> updateSite(
            @PathVariable Long id,
            @Valid @RequestBody SiteCreateRequest request) {
        SiteDTO updated = siteService.updateSite(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSite(@PathVariable Long id) {
        siteService.deleteSite(id);
        return ResponseEntity.noContent().build();
    }
}
