package com.afriasdev.cmms.service;

import com.afriasdev.cmms.dto.CustomerDTO;
import com.afriasdev.cmms.dto.request.CustomerCreateRequest;
import com.afriasdev.cmms.exception.ResourceNotFoundException;
import com.afriasdev.cmms.model.Company;
import com.afriasdev.cmms.model.Customer;
import com.afriasdev.cmms.repository.CompanyRepository;
import com.afriasdev.cmms.repository.CustomerRepository;
import com.afriasdev.cmms.repository.WorkOrderRepository;
import com.afriasdev.cmms.security.model.User;
import com.afriasdev.cmms.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final WorkOrderRepository workOrderRepository;

    public CustomerDTO createCustomer(CustomerCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + request.getUserId()));

        // Verificar que el usuario no sea ya un customer
        customerRepository.findByUserId(request.getUserId())
                .ifPresent(c -> {
                    throw new IllegalArgumentException("El usuario ya es un cliente");
                });

        Customer customer = new Customer();
        customer.setUser(user);
        customer.setPosition(request.getPosition());
        customer.setPhoneAlt(request.getPhoneAlt());
        customer.setNotes(request.getNotes());
        customer.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        if (request.getCompanyId() != null) {
            Company company = companyRepository.findById(request.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada con ID: " + request.getCompanyId()));
            customer.setCompany(company);
        }

        Customer savedCustomer = customerRepository.save(customer);
        return mapToDTO(savedCustomer);
    }

    @Transactional(readOnly = true)
    public CustomerDTO getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));
        return mapToDTO(customer);
    }

    @Transactional(readOnly = true)
    public CustomerDTO getCustomerByUserId(Long userId) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado para el usuario con ID: " + userId));
        return mapToDTO(customer);
    }

    @Transactional(readOnly = true)
    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CustomerDTO> getActiveCustomers() {
        return customerRepository.findByIsActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CustomerDTO> getCustomersByCompany(Long companyId) {
        return customerRepository.findByCompanyId(companyId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public CustomerDTO updateCustomer(Long id, CustomerCreateRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));

        customer.setPosition(request.getPosition());
        customer.setPhoneAlt(request.getPhoneAlt());
        customer.setNotes(request.getNotes());

        if (request.getIsActive() != null) {
            customer.setIsActive(request.getIsActive());
        }

        if (request.getCompanyId() != null) {
            Company company = companyRepository.findById(request.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada con ID: " + request.getCompanyId()));
            customer.setCompany(company);
        }

        Customer updatedCustomer = customerRepository.save(customer);
        return mapToDTO(updatedCustomer);
    }

    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));

        customer.setIsActive(false);
        customerRepository.save(customer);
    }

    private CustomerDTO mapToDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setUserId(customer.getUser().getId());

        User user = customer.getUser();
        dto.setUserName(user.getUsername());
        dto.setUserEmail(user.getEmail());
        dto.setUserFirstName(user.getFirstName());
        dto.setUserLastName(user.getLastName());
        dto.setUserPhone(user.getPhone());

        if (customer.getCompany() != null) {
            dto.setCompanyId(customer.getCompany().getId());
            dto.setCompanyName(customer.getCompany().getName());
        }

        dto.setPosition(customer.getPosition());
        dto.setPhoneAlt(customer.getPhoneAlt());
        dto.setNotes(customer.getNotes());
        dto.setIsActive(customer.getIsActive());
        dto.setCreatedAt(customer.getCreatedAt());
        dto.setUpdatedAt(customer.getUpdatedAt());

        // Contar work orders solicitadas
        long requestedWO = workOrderRepository.findByRequesterId(customer.getUser().getId()).size();
        dto.setRequestedWorkOrders((int) requestedWO);

        return dto;
    }
}