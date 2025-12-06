package com.afriasdev.cmms.service;

import com.afriasdev.cmms.dto.AssetDTO;
import com.afriasdev.cmms.dto.request.AssetFilterRequest;
import com.afriasdev.cmms.dto.response.PaginatedResponse;
import com.afriasdev.cmms.exception.ResourceNotFoundException;
import com.afriasdev.cmms.model.Asset;
import com.afriasdev.cmms.model.AssetCategory;
import com.afriasdev.cmms.model.Site;
import com.afriasdev.cmms.repository.AssetCategoryRepository;
import com.afriasdev.cmms.repository.AssetRepository;
import com.afriasdev.cmms.repository.SiteRepository;
import com.afriasdev.cmms.specification.AssetSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AssetService {
    private final AssetRepository assetRepository;
    private final SiteRepository siteRepository;
    private final AssetCategoryRepository categoryRepository;

    /**
     * Obtener todos los Assets con paginación
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<AssetDTO> getAllAssetsPaginated(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Asset> assetPage = assetRepository.findAll(pageable);

        List<AssetDTO> content = assetPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return new PaginatedResponse<>(
                content,
                assetPage.getNumber(),
                assetPage.getSize(),
                assetPage.getTotalElements()
        );
    }

    /**
     * Filtrar Assets con paginación
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<AssetDTO> filterAssets(AssetFilterRequest filter) {
        Specification<Asset> spec = AssetSpecification.withFilters(
                filter.getCompanyId(),
                filter.getSiteId(),
                filter.getCategoryId(),
                filter.getManufacturer(),
                filter.getModel(),
                filter.getIsActive(),
                filter.getSearch()
        );

        Sort sort = filter.getSortDirection().equalsIgnoreCase("ASC")
                ? Sort.by(filter.getSortBy()).ascending()
                : Sort.by(filter.getSortBy()).descending();

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);
        Page<Asset> assetPage = assetRepository.findAll(spec, pageable);

        List<AssetDTO> content = assetPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return new PaginatedResponse<>(
                content,
                assetPage.getNumber(),
                assetPage.getSize(),
                assetPage.getTotalElements()
        );
    }

    /**
     * Buscar Assets con paginación
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<AssetDTO> searchAssetsPaginated(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Asset> assetPage = assetRepository.searchAssetsPage(search, pageable); // CAMBIO AQUÍ

        List<AssetDTO> content = assetPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return new PaginatedResponse<>(
                content,
                assetPage.getNumber(),
                assetPage.getSize(),
                assetPage.getTotalElements()
        );
    }

    public AssetDTO createAsset(AssetDTO assetDTO) {
        Site site = siteRepository.findById(assetDTO.getSiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Sitio no encontrado con ID: " + assetDTO.getSiteId()));

        // Verificar código único en el sitio
        assetRepository.findBySiteIdAndCode(assetDTO.getSiteId(), assetDTO.getCode())
                .ifPresent(a -> {
                    throw new IllegalArgumentException("Ya existe un activo con el código " + assetDTO.getCode() + " en este sitio");
                });

        Asset asset = new Asset();
        asset.setSite(site);
        asset.setCode(assetDTO.getCode());
        asset.setName(assetDTO.getName());
        asset.setDescription(assetDTO.getDescription());
        asset.setSerialNumber(assetDTO.getSerialNumber());
        asset.setManufacturer(assetDTO.getManufacturer());
        asset.setModel(assetDTO.getModel());
        asset.setInstalledAt(assetDTO.getInstalledAt());
        asset.setIsActive(assetDTO.getIsActive() != null ? assetDTO.getIsActive() : true);

        if (assetDTO.getCategoryId() != null) {
            AssetCategory category = categoryRepository.findById(assetDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + assetDTO.getCategoryId()));
            asset.setCategory(category);
        }

        Asset savedAsset = assetRepository.save(asset);
        return mapToDTO(savedAsset);
    }

    @Transactional(readOnly = true)
    public AssetDTO getAssetById(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activo no encontrado con ID: " + id));
        return mapToDTO(asset);
    }

    @Transactional(readOnly = true)
    public List<AssetDTO> getAllAssets() {
        return assetRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AssetDTO> getAssetsBySite(Long siteId) {
        return assetRepository.findBySiteId(siteId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AssetDTO> getAssetsByCompany(Long companyId) {
        return assetRepository.findByCompanyId(companyId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AssetDTO> searchAssets(String search) {
        return assetRepository.searchAssets(search).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public AssetDTO updateAsset(Long id, AssetDTO assetDTO) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activo no encontrado con ID: " + id));

        asset.setName(assetDTO.getName());
        asset.setDescription(assetDTO.getDescription());
        asset.setSerialNumber(assetDTO.getSerialNumber());
        asset.setManufacturer(assetDTO.getManufacturer());
        asset.setModel(assetDTO.getModel());
        asset.setInstalledAt(assetDTO.getInstalledAt());

        if (assetDTO.getIsActive() != null) {
            asset.setIsActive(assetDTO.getIsActive());
        }

        if (assetDTO.getCategoryId() != null) {
            AssetCategory category = categoryRepository.findById(assetDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + assetDTO.getCategoryId()));
            asset.setCategory(category);
        }

        Asset updatedAsset = assetRepository.save(asset);
        return mapToDTO(updatedAsset);
    }

    public void deleteAsset(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activo no encontrado con ID: " + id));

        asset.setIsActive(false);
        assetRepository.save(asset);
    }

    private AssetDTO mapToDTO(Asset asset) {
        AssetDTO dto = new AssetDTO();
        dto.setId(asset.getId());
        dto.setSiteId(asset.getSite().getId());
        dto.setSiteName(asset.getSite().getName());
        dto.setCompanyName(asset.getSite().getCompany().getName());

        if (asset.getCategory() != null) {
            dto.setCategoryId(asset.getCategory().getId());
            dto.setCategoryName(asset.getCategory().getName());
        }

        dto.setCode(asset.getCode());
        dto.setName(asset.getName());
        dto.setDescription(asset.getDescription());
        dto.setSerialNumber(asset.getSerialNumber());
        dto.setManufacturer(asset.getManufacturer());
        dto.setModel(asset.getModel());
        dto.setInstalledAt(asset.getInstalledAt());
        dto.setIsActive(asset.getIsActive());
        dto.setCreatedAt(asset.getCreatedAt());
        dto.setUpdatedAt(asset.getUpdatedAt());

        return dto;
    }
}
