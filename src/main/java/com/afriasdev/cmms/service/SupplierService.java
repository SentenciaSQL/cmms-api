package com.afriasdev.cmms.service;

import com.afriasdev.cmms.dto.AddressDTO;
import com.afriasdev.cmms.dto.SupplierDTO;
import com.afriasdev.cmms.dto.request.SupplierCreateRequest;
import com.afriasdev.cmms.exception.ResourceNotFoundException;
import com.afriasdev.cmms.model.Supplier;
import com.afriasdev.cmms.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;

    @Transactional
    public SupplierDTO create(SupplierCreateRequest request) {
        Supplier supplier = new Supplier();
        supplier.setName(request.getName());
        supplier.setCode(request.getCode());
        supplier.setDescription(request.getDescription());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setMobile(request.getMobile());
        supplier.setWebsite(request.getWebsite());
        supplier.setTaxId(request.getTaxId());

        if (request.getAddress() != null) {
            Supplier.Address address = new Supplier.Address();
            address.setStreet(request.getAddress().getStreet());
            address.setCity(request.getAddress().getCity());
            address.setState(request.getAddress().getState());
            address.setPostalCode(request.getAddress().getPostalCode());
            address.setCountry(request.getAddress().getCountry());
            supplier.setAddress(address);
        }

        supplier.setContactPerson(request.getContactPerson());
        supplier.setContactEmail(request.getContactEmail());
        supplier.setContactPhone(request.getContactPhone());
        supplier.setSupplierType(request.getSupplierType());
        supplier.setNotes(request.getNotes());

        Supplier saved = supplierRepository.save(supplier);
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<SupplierDTO> findAll() {
        return supplierRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SupplierDTO findById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));
        return toDTO(supplier);
    }

    @Transactional(readOnly = true)
    public List<SupplierDTO> findActive() {
        return supplierRepository.findByIsActiveTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SupplierDTO> search(String query) {
        return supplierRepository.searchSuppliers(query).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public SupplierDTO update(Long id, SupplierCreateRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));

        supplier.setName(request.getName());
        supplier.setCode(request.getCode());
        supplier.setDescription(request.getDescription());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setMobile(request.getMobile());
        supplier.setWebsite(request.getWebsite());
        supplier.setTaxId(request.getTaxId());

        if (request.getAddress() != null) {
            Supplier.Address address = new Supplier.Address();
            address.setStreet(request.getAddress().getStreet());
            address.setCity(request.getAddress().getCity());
            address.setState(request.getAddress().getState());
            address.setPostalCode(request.getAddress().getPostalCode());
            address.setCountry(request.getAddress().getCountry());
            supplier.setAddress(address);
        }

        supplier.setContactPerson(request.getContactPerson());
        supplier.setContactEmail(request.getContactEmail());
        supplier.setContactPhone(request.getContactPhone());
        supplier.setSupplierType(request.getSupplierType());
        supplier.setNotes(request.getNotes());

        Supplier saved = supplierRepository.save(supplier);
        return toDTO(saved);
    }

    @Transactional
    public void delete(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));
        supplier.setIsActive(false);
        supplierRepository.save(supplier);
    }

    private SupplierDTO toDTO(Supplier supplier) {
        SupplierDTO dto = new SupplierDTO();
        dto.setId(supplier.getId());
        dto.setName(supplier.getName());
        dto.setCode(supplier.getCode());
        dto.setDescription(supplier.getDescription());
        dto.setEmail(supplier.getEmail());
        dto.setPhone(supplier.getPhone());
        dto.setMobile(supplier.getMobile());
        dto.setWebsite(supplier.getWebsite());
        dto.setTaxId(supplier.getTaxId());

        if (supplier.getAddress() != null) {
            AddressDTO addressDTO = new AddressDTO();
            addressDTO.setStreet(supplier.getAddress().getStreet());
            addressDTO.setCity(supplier.getAddress().getCity());
            addressDTO.setState(supplier.getAddress().getState());
            addressDTO.setPostalCode(supplier.getAddress().getPostalCode());
            addressDTO.setCountry(supplier.getAddress().getCountry());
            dto.setAddress(addressDTO);
        }

        dto.setContactPerson(supplier.getContactPerson());
        dto.setContactEmail(supplier.getContactEmail());
        dto.setContactPhone(supplier.getContactPhone());
        dto.setSupplierType(supplier.getSupplierType());
        dto.setNotes(supplier.getNotes());
        dto.setIsActive(supplier.getIsActive());
        dto.setCreatedAt(supplier.getCreatedAt());
        dto.setUpdatedAt(supplier.getUpdatedAt());

        return dto;
    }
}