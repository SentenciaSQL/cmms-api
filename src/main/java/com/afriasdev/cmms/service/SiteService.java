package com.afriasdev.cmms.service;

import com.afriasdev.cmms.dto.SiteDTO;
import com.afriasdev.cmms.dto.request.SiteCreateRequest;
import com.afriasdev.cmms.exception.ResourceNotFoundException;
import com.afriasdev.cmms.model.Company;
import com.afriasdev.cmms.model.Site;
import com.afriasdev.cmms.repository.CompanyRepository;
import com.afriasdev.cmms.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SiteService {
    private final SiteRepository siteRepository;
    private final CompanyRepository companyRepository;

    public SiteDTO createSite(SiteCreateRequest request) {
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada con ID: " + request.getCompanyId()));

        // Verificar código único si se proporciona
        if (request.getCode() != null && !request.getCode().isEmpty()) {
            siteRepository.findByCompanyIdAndCode(request.getCompanyId(), request.getCode())
                    .ifPresent(s -> {
                        throw new IllegalArgumentException("Ya existe un sitio con el código " + request.getCode() + " en esta empresa");
                    });
        }

        Site site = new Site();
        site.setCompany(company);
        site.setName(request.getName());
        site.setCode(request.getCode());
        site.setAddress(request.getAddress());
        site.setCity(request.getCity());
        site.setState(request.getState());
        site.setCountry(request.getCountry());
        site.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        Site savedSite = siteRepository.save(site);
        return mapToDTO(savedSite);
    }

    @Transactional(readOnly = true)
    public SiteDTO getSiteById(Long id) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sitio no encontrado con ID: " + id));
        return mapToDTO(site);
    }

    @Transactional(readOnly = true)
    public List<SiteDTO> getAllSites() {
        return siteRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SiteDTO> getActiveSites() {
        return siteRepository.findByIsActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SiteDTO> getSitesByCompany(Long companyId) {
        return siteRepository.findByCompanyId(companyId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SiteDTO> getActiveSitesByCompany(Long companyId) {
        return siteRepository.findByCompanyIdAndIsActiveTrue(companyId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public SiteDTO updateSite(Long id, SiteCreateRequest request) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sitio no encontrado con ID: " + id));

        // Si cambia el código, verificar que sea único
        if (request.getCode() != null && !request.getCode().equals(site.getCode())) {
            siteRepository.findByCompanyIdAndCode(site.getCompany().getId(), request.getCode())
                    .ifPresent(s -> {
                        throw new IllegalArgumentException("Ya existe un sitio con el código " + request.getCode() + " en esta empresa");
                    });
        }

        site.setName(request.getName());
        site.setCode(request.getCode());
        site.setAddress(request.getAddress());
        site.setCity(request.getCity());
        site.setState(request.getState());
        site.setCountry(request.getCountry());

        if (request.getIsActive() != null) {
            site.setIsActive(request.getIsActive());
        }

        Site updatedSite = siteRepository.save(site);
        return mapToDTO(updatedSite);
    }

    public void deleteSite(Long id) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sitio no encontrado con ID: " + id));

        site.setIsActive(false);
        siteRepository.save(site);
    }

    private SiteDTO mapToDTO(Site site) {
        SiteDTO dto = new SiteDTO();
        dto.setId(site.getId());
        dto.setCompanyId(site.getCompany().getId());
        dto.setCompanyName(site.getCompany().getName());
        dto.setName(site.getName());
        dto.setCode(site.getCode());
        dto.setAddress(site.getAddress());
        dto.setCity(site.getCity());
        dto.setState(site.getState());
        dto.setCountry(site.getCountry());
        dto.setIsActive(site.getIsActive());
        dto.setCreatedAt(site.getCreatedAt());
        dto.setUpdatedAt(site.getUpdatedAt());

        // Contadores
        if (site.getAssets() != null) {
            dto.setTotalAssets(site.getAssets().size());
        }

        if (site.getWorkOrders() != null) {
            long activeWO = site.getWorkOrders().stream()
                    .filter(wo -> wo.getStatus().toString().equals("OPEN") || wo.getStatus().toString().equals("IN_PROGRESS"))
                    .count();
            dto.setActiveWorkOrders((int) activeWO);
        }

        return dto;
    }
}
