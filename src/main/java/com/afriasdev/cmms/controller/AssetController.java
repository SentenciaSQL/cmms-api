package com.afriasdev.cmms.controller;

import com.afriasdev.cmms.dto.AssetDTO;
import com.afriasdev.cmms.dto.request.AssetFilterRequest;
import com.afriasdev.cmms.dto.response.PaginatedResponse;
import com.afriasdev.cmms.service.AssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Assets", description = "Gestión de Activos ")
public class AssetController {

    private final AssetService assetService;

    /**
     * Listar con paginación
     */
    @GetMapping("/paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Listar activos con paginación y ordenamiento")
    public ResponseEntity<PaginatedResponse<AssetDTO>> getAllAssetsPaginated(
            @Parameter(description = "Número de página (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo para ordenar") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Dirección (ASC/DESC)") @RequestParam(defaultValue = "ASC") String sortDirection) {

        PaginatedResponse<AssetDTO> response = assetService.getAllAssetsPaginated(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }

    /**
     * Filtrar con paginación
     */
    @PostMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    @Operation(
            summary = "Filtrar activos con múltiples criterios",
            description = """
            Filtros disponibles:
            - companyId, siteId, categoryId
            - manufacturer, model
            - isActive: true/false
            - search: Buscar en nombre, código o serial
            - page, size, sortBy, sortDirection
            """
    )
    public ResponseEntity<PaginatedResponse<AssetDTO>> filterAssets(
            @RequestBody AssetFilterRequest filter) {

        PaginatedResponse<AssetDTO> response = assetService.filterAssets(filter);
        return ResponseEntity.ok(response);
    }

    /**
     * Buscar con paginación
     */
    @GetMapping("/search/paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Buscar activos en nombre, código o número de serie")
    public ResponseEntity<PaginatedResponse<AssetDTO>> searchAssetsPaginated(
            @Parameter(description = "Texto a buscar") @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PaginatedResponse<AssetDTO> response = assetService.searchAssetsPaginated(query, page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<AssetDTO> createAsset(@Valid @RequestBody AssetDTO assetDTO) {
        AssetDTO created = assetService.createAsset(assetDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    public ResponseEntity<List<AssetDTO>> getAllAssets() {
        List<AssetDTO> assets = assetService.getAllAssets();
        return ResponseEntity.ok(assets);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    public ResponseEntity<AssetDTO> getAssetById(@PathVariable Long id) {
        AssetDTO asset = assetService.getAssetById(id);
        return ResponseEntity.ok(asset);
    }

    @GetMapping("/site/{siteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    public ResponseEntity<List<AssetDTO>> getAssetsBySite(@PathVariable Long siteId) {
        List<AssetDTO> assets = assetService.getAssetsBySite(siteId);
        return ResponseEntity.ok(assets);
    }

    @GetMapping("/company/{companyId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<AssetDTO>> getAssetsByCompany(@PathVariable Long companyId) {
        List<AssetDTO> assets = assetService.getAssetsByCompany(companyId);
        return ResponseEntity.ok(assets);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    public ResponseEntity<List<AssetDTO>> searchAssets(@RequestParam String query) {
        List<AssetDTO> assets = assetService.searchAssets(query);
        return ResponseEntity.ok(assets);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<AssetDTO> updateAsset(
            @PathVariable Long id,
            @Valid @RequestBody AssetDTO assetDTO) {
        AssetDTO updated = assetService.updateAsset(id, assetDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAsset(@PathVariable Long id) {
        assetService.deleteAsset(id);
        return ResponseEntity.noContent().build();
    }
}