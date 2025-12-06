package com.afriasdev.cmms.mapper;

import com.afriasdev.cmms.dto.CompanyDTO;
import com.afriasdev.cmms.model.Company;
import org.springframework.stereotype.Component;

@Component
public class CompanyMapper {

    public CompanyDTO toDTO(Company company) {
        if (company == null) return null;

        CompanyDTO dto = new CompanyDTO();
        dto.setId(company.getId());
        dto.setName(company.getName());
        dto.setTaxId(company.getTaxId());
        dto.setPhone(company.getPhone());
        dto.setEmail(company.getEmail());
        dto.setAddress(company.getAddress());
        dto.setIsActive(company.getIsActive());
        dto.setCreatedAt(company.getCreatedAt());
        dto.setUpdatedAt(company.getUpdatedAt());

        // Contadores opcionales
        if (company.getSites() != null) {
            dto.setTotalSites(company.getSites().size());
        }

        return dto;
    }

    public Company toEntity(CompanyDTO dto) {
        if (dto == null) return null;

        Company company = new Company();
        company.setId(dto.getId());
        company.setName(dto.getName());
        company.setTaxId(dto.getTaxId());
        company.setPhone(dto.getPhone());
        company.setEmail(dto.getEmail());
        company.setAddress(dto.getAddress());
        company.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

        return company;
    }

    public void updateEntityFromDTO(CompanyDTO dto, Company company) {
        if (dto == null || company == null) return;

        company.setName(dto.getName());
        company.setTaxId(dto.getTaxId());
        company.setPhone(dto.getPhone());
        company.setEmail(dto.getEmail());
        company.setAddress(dto.getAddress());

        if (dto.getIsActive() != null) {
            company.setIsActive(dto.getIsActive());
        }
    }
}
