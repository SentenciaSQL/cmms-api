package com.afriasdev.cmms.service;

import com.afriasdev.cmms.dto.AssetCategoryDTO;
import com.afriasdev.cmms.dto.request.AssetCategoryCreateRequest;
import com.afriasdev.cmms.exception.ResourceNotFoundException;
import com.afriasdev.cmms.model.AssetCategory;
import com.afriasdev.cmms.repository.AssetCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AssetCategoryService {

    private final AssetCategoryRepository categoryRepository;

    public AssetCategoryDTO createCategory(AssetCategoryCreateRequest request) {
        // Verificar código único si se proporciona
        if (request.getCode() != null && !request.getCode().isEmpty()) {
            categoryRepository.findByCode(request.getCode())
                    .ifPresent(c -> {
                        throw new IllegalArgumentException("Ya existe una categoría con el código: " + request.getCode());
                    });
        }

        AssetCategory category = new AssetCategory();
        category.setName(request.getName());
        category.setCode(request.getCode());
        category.setDescription(request.getDescription());
        category.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        AssetCategory savedCategory = categoryRepository.save(category);
        return mapToDTO(savedCategory);
    }

    @Transactional(readOnly = true)
    public AssetCategoryDTO getCategoryById(Long id) {
        AssetCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));
        return mapToDTO(category);
    }

    @Transactional(readOnly = true)
    public AssetCategoryDTO getCategoryByCode(String code) {
        AssetCategory category = categoryRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con código: " + code));
        return mapToDTO(category);
    }

    @Transactional(readOnly = true)
    public List<AssetCategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AssetCategoryDTO> getActiveCategories() {
        return categoryRepository.findByIsActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AssetCategoryDTO> searchCategories(String name) {
        return categoryRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public AssetCategoryDTO updateCategory(Long id, AssetCategoryCreateRequest request) {
        AssetCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));

        // Si cambia el código, verificar que sea único
        if (request.getCode() != null && !request.getCode().equals(category.getCode())) {
            categoryRepository.findByCode(request.getCode())
                    .ifPresent(c -> {
                        throw new IllegalArgumentException("Ya existe una categoría con el código: " + request.getCode());
                    });
        }

        category.setName(request.getName());
        category.setCode(request.getCode());
        category.setDescription(request.getDescription());

        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }

        AssetCategory updatedCategory = categoryRepository.save(category);
        return mapToDTO(updatedCategory);
    }

    public void deleteCategory(Long id) {
        AssetCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));

        // Verificar si tiene activos asociados
        if (category.getAssets() != null && !category.getAssets().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar la categoría porque tiene activos asociados");
        }

        category.setIsActive(false);
        categoryRepository.save(category);
    }

    public void hardDeleteCategory(Long id) {
        AssetCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));

        // Verificar si tiene activos asociados
        if (category.getAssets() != null && !category.getAssets().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar la categoría porque tiene activos asociados");
        }

        categoryRepository.delete(category);
    }

    private AssetCategoryDTO mapToDTO(AssetCategory category) {
        AssetCategoryDTO dto = new AssetCategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setCode(category.getCode());
        dto.setDescription(category.getDescription());
        dto.setIsActive(category.getIsActive());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());

        if (category.getAssets() != null) {
            dto.setTotalAssets(category.getAssets().size());
        }

        return dto;
    }
}