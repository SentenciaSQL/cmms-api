package com.afriasdev.cmms.service;

import com.afriasdev.cmms.dto.TechnicianDTO;
import com.afriasdev.cmms.exception.ResourceNotFoundException;
import com.afriasdev.cmms.model.Technician;
import com.afriasdev.cmms.model.WorkOrderStatus;
import com.afriasdev.cmms.repository.TechnicianRepository;
import com.afriasdev.cmms.repository.WorkOrderRepository;
import com.afriasdev.cmms.security.model.User;
import com.afriasdev.cmms.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TechnicianService {

    private final TechnicianRepository technicianRepository;
    private final UserRepository userRepository;
    private final WorkOrderRepository workOrderRepository;

    public TechnicianDTO createTechnician(TechnicianDTO technicianDTO) {
        User user = userRepository.findById(technicianDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + technicianDTO.getUserId()));

        // Verificar que el usuario no sea ya un técnico
        technicianRepository.findByUserId(technicianDTO.getUserId())
                .ifPresent(t -> {
                    throw new IllegalArgumentException("El usuario ya es un técnico");
                });

        Technician technician = new Technician();
        technician.setUser(user);
        technician.setSkillLevel(technicianDTO.getSkillLevel());
        technician.setHourlyRate(technicianDTO.getHourlyRate());
        technician.setPhoneAlt(technicianDTO.getPhoneAlt());
        technician.setNotes(technicianDTO.getNotes());
        technician.setIsActive(technicianDTO.getIsActive() != null ? technicianDTO.getIsActive() : true);

        Technician savedTechnician = technicianRepository.save(technician);
        return mapToDTO(savedTechnician);
    }

    @Transactional(readOnly = true)
    public TechnicianDTO getTechnicianById(Long id) {
        Technician technician = technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Técnico no encontrado con ID: " + id));
        return mapToDTO(technician);
    }

    @Transactional(readOnly = true)
    public TechnicianDTO getTechnicianByUserId(Long userId) {
        Technician technician = technicianRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Técnico no encontrado para el usuario con ID: " + userId));
        return mapToDTO(technician);
    }

    @Transactional(readOnly = true)
    public List<TechnicianDTO> getAllTechnicians() {
        return technicianRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TechnicianDTO> getActiveTechnicians() {
        return technicianRepository.findByIsActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TechnicianDTO> getAvailableTechnicians(int maxWorkOrders) {
        return technicianRepository.findAvailableTechnicians(maxWorkOrders).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TechnicianDTO> getTechniciansBySkillLevel(String skillLevel) {
        return technicianRepository.findBySkillLevel(skillLevel).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public TechnicianDTO updateTechnician(Long id, TechnicianDTO technicianDTO) {
        Technician technician = technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Técnico no encontrado con ID: " + id));

        technician.setSkillLevel(technicianDTO.getSkillLevel());
        technician.setHourlyRate(technicianDTO.getHourlyRate());
        technician.setPhoneAlt(technicianDTO.getPhoneAlt());
        technician.setNotes(technicianDTO.getNotes());

        if (technicianDTO.getIsActive() != null) {
            technician.setIsActive(technicianDTO.getIsActive());
        }

        Technician updated = technicianRepository.save(technician);
        return mapToDTO(updated);
    }

    public void deleteTechnician(Long id) {
        Technician technician = technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Técnico no encontrado con ID: " + id));

        technician.setIsActive(false);
        technicianRepository.save(technician);
    }

    private TechnicianDTO mapToDTO(Technician technician) {
        TechnicianDTO dto = new TechnicianDTO();
        dto.setId(technician.getId());
        dto.setUserId(technician.getUser().getId());

        User user = technician.getUser();
        dto.setUserName(user.getUsername());
        dto.setUserEmail(user.getEmail());
        dto.setUserFirstName(user.getFirstName());
        dto.setUserLastName(user.getLastName());
        dto.setUserPhone(user.getPhone());

        dto.setSkillLevel(technician.getSkillLevel());
        dto.setHourlyRate(technician.getHourlyRate());
        dto.setPhoneAlt(technician.getPhoneAlt());
        dto.setNotes(technician.getNotes());
        dto.setIsActive(technician.getIsActive());
        dto.setCreatedAt(technician.getCreatedAt());
        dto.setUpdatedAt(technician.getUpdatedAt());

        // Calcular métricas
        Long assignedCount = workOrderRepository.countActiveWorkOrdersByTechId(technician.getId());
        dto.setAssignedWorkOrders(assignedCount != null ? assignedCount.intValue() : 0);

        long completedCount = workOrderRepository.findByAssignedTechId(technician.getId()).stream()
                .filter(wo -> wo.getStatus() == WorkOrderStatus.COMPLETED)
                .count();
        dto.setCompletedWorkOrders((int) completedCount);

        BigDecimal totalHours = workOrderRepository.findByAssignedTechId(technician.getId()).stream()
                .filter(wo -> wo.getActualHours() != null)
                .map(wo -> wo.getActualHours())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalHoursWorked(totalHours);

        return dto;
    }
}