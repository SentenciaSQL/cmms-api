package com.afriasdev.cmms.controller;

import com.afriasdev.cmms.dto.AssetCategoryDTO;
import com.afriasdev.cmms.dto.request.AssetCategoryCreateRequest;
import com.afriasdev.cmms.service.AssetCategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asset-categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Assets Category", description = "Gestión de Categía de Activos ")
public class AssetCategoryController {
    private final AssetCategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<AssetCategoryDTO> createCategory(@Valid @RequestBody AssetCategoryCreateRequest request) {
        AssetCategoryDTO created = categoryService.createCategory(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    public ResponseEntity<List<AssetCategoryDTO>> getAllCategories() {
        List<AssetCategoryDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    public ResponseEntity<AssetCategoryDTO> getCategoryById(@PathVariable Long id) {
        AssetCategoryDTO category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    public ResponseEntity<AssetCategoryDTO> getCategoryByCode(@PathVariable String code) {
        AssetCategoryDTO category = categoryService.getCategoryByCode(code);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    public ResponseEntity<List<AssetCategoryDTO>> getActiveCategories() {
        List<AssetCategoryDTO> categories = categoryService.getActiveCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    public ResponseEntity<List<AssetCategoryDTO>> searchCategories(@RequestParam String name) {
        List<AssetCategoryDTO> categories = categoryService.searchCategories(name);
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<AssetCategoryDTO> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody AssetCategoryCreateRequest request) {
        AssetCategoryDTO updated = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/hard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> hardDeleteCategory(@PathVariable Long id) {
        categoryService.hardDeleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
