package com.afriasdev.cmms.service;

import com.afriasdev.cmms.dto.CompanyDTO;
import com.afriasdev.cmms.exception.ResourceNotFoundException;
import com.afriasdev.cmms.mapper.CompanyMapper;
import com.afriasdev.cmms.model.Company;
import com.afriasdev.cmms.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    public CompanyDTO createCompany(CompanyDTO companyDTO) {
        Company company = companyMapper.toEntity(companyDTO);
        Company savedCompany = companyRepository.save(company);
        return companyMapper.toDTO(savedCompany);
    }

    @Transactional(readOnly = true)
    public CompanyDTO getCompanyById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada con ID: " + id));
        return companyMapper.toDTO(company);
    }

    @Transactional(readOnly = true)
    public List<CompanyDTO> getAllCompanies() {
        return companyRepository.findAll().stream()
                .map(companyMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CompanyDTO> getActiveCompanies() {
        return companyRepository.findByIsActiveTrue().stream()
                .map(companyMapper::toDTO)
                .collect(Collectors.toList());
    }

    public CompanyDTO updateCompany(Long id, CompanyDTO companyDTO) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada con ID: " + id));

        companyMapper.updateEntityFromDTO(companyDTO, company);
        Company updatedCompany = companyRepository.save(company);

        return companyMapper.toDTO(updatedCompany);
    }

    public void deleteCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada con ID: " + id));

        company.setIsActive(false);
        companyRepository.save(company);
    }

    @Transactional(readOnly = true)
    public List<CompanyDTO> searchCompanies(String name) {
        return companyRepository.findByNameContainingIgnoreCase(name).stream()
                .map(companyMapper::toDTO)
                .collect(Collectors.toList());
    }
}
