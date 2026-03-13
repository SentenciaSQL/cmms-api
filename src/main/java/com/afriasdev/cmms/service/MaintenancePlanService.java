package com.afriasdev.cmms.service;

import com.afriasdev.cmms.dto.AssetDTO;
import com.afriasdev.cmms.dto.MaintenancePlanDTO;
import com.afriasdev.cmms.dto.TechnicianDTO;
import com.afriasdev.cmms.dto.request.MaintenancePlanCreateRequest;
import com.afriasdev.cmms.dto.request.MaintenancePlanUpdateRequest;
import com.afriasdev.cmms.exception.ResourceNotFoundException;
import com.afriasdev.cmms.model.Asset;
import com.afriasdev.cmms.model.MaintenancePlan;
import com.afriasdev.cmms.model.Technician;
import com.afriasdev.cmms.model.WorkOrder;
import com.afriasdev.cmms.model.WorkOrderPriority;
import com.afriasdev.cmms.model.WorkOrderStatus;
import com.afriasdev.cmms.repository.AssetRepository;
import com.afriasdev.cmms.repository.MaintenancePlanRepository;
import com.afriasdev.cmms.repository.TechnicianRepository;
import com.afriasdev.cmms.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaintenancePlanService {

    private final MaintenancePlanRepository maintenancePlanRepository;
    private final AssetRepository assetRepository;
    private final TechnicianRepository technicianRepository;
    private final WorkOrderRepository workOrderRepository;

    @Transactional
    public MaintenancePlanDTO create(MaintenancePlanCreateRequest request) {
        Asset asset = assetRepository.findById(request.getAssetId())
                .orElseThrow(() -> new ResourceNotFoundException("Activo no encontrado"));

        MaintenancePlan plan = new MaintenancePlan();
        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setAsset(asset);
        plan.setType(request.getType());
        plan.setFrequency(request.getFrequency());
        plan.setFrequencyValue(request.getFrequencyValue());
        plan.setNextScheduledDate(request.getNextScheduledDate());
        plan.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());
        plan.setPriority(request.getPriority());
        plan.setInstructions(request.getInstructions());
        plan.setAutoGenerateWorkOrder(request.getAutoGenerateWorkOrder());

        if (request.getAssignedTechnicianId() != null) {
            Technician technician = technicianRepository.findById(request.getAssignedTechnicianId())
                    .orElseThrow(() -> new ResourceNotFoundException("Técnico no encontrado"));
            plan.setAssignedTechnician(technician);
        }

        MaintenancePlan saved = maintenancePlanRepository.save(plan);
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<MaintenancePlanDTO> findAll() {
        return maintenancePlanRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MaintenancePlanDTO findById(Long id) {
        MaintenancePlan plan = maintenancePlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan no encontrado"));
        return toDTO(plan);
    }

    @Transactional(readOnly = true)
    public List<MaintenancePlanDTO> findByAssetId(Long assetId) {
        return maintenancePlanRepository.findByAssetId(assetId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MaintenancePlanDTO> findDuePlans() {
        return maintenancePlanRepository.findDueMaintenancePlans(LocalDateTime.now()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public MaintenancePlanDTO update(Long id, MaintenancePlanUpdateRequest request) {
        MaintenancePlan plan = maintenancePlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan no encontrado"));

        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setType(request.getType());
        plan.setFrequency(request.getFrequency());
        plan.setFrequencyValue(request.getFrequencyValue());
        plan.setNextScheduledDate(request.getNextScheduledDate());
        plan.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());
        plan.setPriority(request.getPriority());
        plan.setInstructions(request.getInstructions());
        plan.setAutoGenerateWorkOrder(request.getAutoGenerateWorkOrder());

        if (request.getAssignedTechnicianId() != null) {
            Technician technician = technicianRepository.findById(request.getAssignedTechnicianId())
                    .orElseThrow(() -> new ResourceNotFoundException("Técnico no encontrado"));
            plan.setAssignedTechnician(technician);
        }

        MaintenancePlan saved = maintenancePlanRepository.save(plan);
        return toDTO(saved);
    }

    @Transactional
    public void delete(Long id) {
        MaintenancePlan plan = maintenancePlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan no encontrado"));
        plan.setIsActive(false);
        maintenancePlanRepository.save(plan);
    }

    /**
     * Ejecucion manual de un plan: genera la OT inmediatamente sin esperar al scheduler.
     * Util desde el endpoint POST /api/maintenance-plans/{id}/execute.
     */
    @Transactional
    public void executeMaintenancePlan(Long id) {
        MaintenancePlan plan = maintenancePlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan no encontrado"));

        LocalDateTime now = LocalDateTime.now();
        long count = workOrderRepository.count();

        WorkOrder workOrder = new WorkOrder();
        workOrder.setCode(String.format("WO-%05d", count + 1));
        workOrder.setTitle(String.format("[%s] %s", plan.getType().name(), plan.getName()));
        workOrder.setDescription(plan.getInstructions());
        workOrder.setStatus(WorkOrderStatus.OPEN);
        workOrder.setPriority(mapPlanPriority(plan.getPriority()));
        workOrder.setAsset(plan.getAsset());

        if (plan.getAsset() != null && plan.getAsset().getSite() != null) {
            workOrder.setSite(plan.getAsset().getSite());
            if (plan.getAsset().getSite().getCompany() != null) {
                workOrder.setCompany(plan.getAsset().getSite().getCompany());
            }
        }

        LocalDateTime scheduledStart = plan.getNextScheduledDate() != null
                ? plan.getNextScheduledDate() : now;
        workOrder.setScheduledStart(scheduledStart);
        workOrder.setScheduledEnd(scheduledStart.plusMinutes(plan.getEstimatedDurationMinutes()));
        workOrder.setDueDate(scheduledStart.toLocalDate());
        workOrder.setEstimatedHours(
                java.math.BigDecimal.valueOf(plan.getEstimatedDurationMinutes())
                        .divide(java.math.BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP));

        if (plan.getAssignedTechnician() != null) {
            workOrder.setAssignedTech(plan.getAssignedTechnician());
        }

        workOrderRepository.save(workOrder);

        plan.setLastExecutionDate(now);
        plan.setNextScheduledDate(calculateNextDate(plan));
        maintenancePlanRepository.save(plan);
    }

    private WorkOrderPriority mapPlanPriority(MaintenancePlan.Priority priority) {
        return switch (priority) {
            case LOW      -> WorkOrderPriority.LOW;
            case MEDIUM   -> WorkOrderPriority.MEDIUM;
            case HIGH     -> WorkOrderPriority.HIGH;
            case CRITICAL -> WorkOrderPriority.URGENT;
        };
    }

    private LocalDateTime calculateNextDate(MaintenancePlan plan) {
        LocalDateTime base = LocalDateTime.now();
        switch (plan.getFrequency()) {
            case DAILY:
                return base.plusDays(plan.getFrequencyValue());
            case WEEKLY:
                return base.plusWeeks(plan.getFrequencyValue());
            case MONTHLY:
                return base.plusMonths(plan.getFrequencyValue());
            case QUARTERLY:
                return base.plusMonths(plan.getFrequencyValue() * 3L);
            case YEARLY:
                return base.plusYears(plan.getFrequencyValue());
            default:
                return base.plusMonths(1);
        }
    }

    private MaintenancePlanDTO toDTO(MaintenancePlan plan) {
        MaintenancePlanDTO dto = new MaintenancePlanDTO();
        dto.setId(plan.getId());
        dto.setName(plan.getName());
        dto.setDescription(plan.getDescription());

        // Asset - Usar tu AssetDTO existente
        if (plan.getAsset() != null) {
            AssetDTO assetDTO = new AssetDTO();
            assetDTO.setId(plan.getAsset().getId());
            assetDTO.setName(plan.getAsset().getName());
            assetDTO.setCode(plan.getAsset().getCode());
            // Completa con los campos que tenga tu AssetDTO
            dto.setAsset(assetDTO);
        }

        // Technician - Usar tu TechnicianDTO existente
        if (plan.getAssignedTechnician() != null) {
            TechnicianDTO techDTO = new TechnicianDTO();
            techDTO.setId(plan.getAssignedTechnician().getId());
            // Completa con los campos que tenga tu TechnicianDTO
            dto.setAssignedTechnician(techDTO);
        }

        dto.setType(plan.getType());
        dto.setFrequency(plan.getFrequency());
        dto.setFrequencyValue(plan.getFrequencyValue());
        dto.setNextScheduledDate(plan.getNextScheduledDate());
        dto.setLastExecutionDate(plan.getLastExecutionDate());
        dto.setEstimatedDurationMinutes(plan.getEstimatedDurationMinutes());
        dto.setPriority(plan.getPriority());
        dto.setInstructions(plan.getInstructions());
        dto.setIsActive(plan.getIsActive());
        dto.setAutoGenerateWorkOrder(plan.getAutoGenerateWorkOrder());
        dto.setCreatedAt(plan.getCreatedAt());
        dto.setUpdatedAt(plan.getUpdatedAt());

        return dto;
    }
}