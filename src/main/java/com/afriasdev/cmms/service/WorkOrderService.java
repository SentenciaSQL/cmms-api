package com.afriasdev.cmms.service;

import com.afriasdev.cmms.dto.WorkOrderDTO;
import com.afriasdev.cmms.dto.request.WorkOrderCreateRequest;
import com.afriasdev.cmms.dto.request.WorkOrderFilterRequest;
import com.afriasdev.cmms.dto.request.WorkOrderUpdateStatusRequest;
import com.afriasdev.cmms.dto.response.PaginatedResponse;
import com.afriasdev.cmms.exception.ResourceNotFoundException;
import com.afriasdev.cmms.model.*;
import com.afriasdev.cmms.repository.*;
import com.afriasdev.cmms.security.model.User;
import com.afriasdev.cmms.security.repository.UserRepository;
import com.afriasdev.cmms.specification.WorkOrderSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkOrderService {
    private final WorkOrderRepository workOrderRepository;
    private final UserRepository userRepository;
    private final TechnicianRepository technicianRepository;
    private final AssetRepository assetRepository;
    private final SiteRepository siteRepository;
    private final CompanyRepository companyRepository;


    /**
     * Obtener todas las Work Orders con paginación
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<WorkOrderDTO> getAllWorkOrdersPaginated(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<WorkOrder> workOrderPage = workOrderRepository.findAll(pageable);

        List<WorkOrderDTO> content = workOrderPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return new PaginatedResponse<>(
                content,
                workOrderPage.getNumber(),
                workOrderPage.getSize(),
                workOrderPage.getTotalElements()
        );
    }

    /**
     * Filtrar Work Orders con paginación
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<WorkOrderDTO> filterWorkOrders(WorkOrderFilterRequest filter) {
        Specification<WorkOrder> spec = WorkOrderSpecification.withFilters(
                filter.getStatuses(),
                filter.getPriorities(),
                filter.getCompanyId(),
                filter.getSiteId(),
                filter.getAssetId(),
                filter.getAssignedTechId(),
                filter.getRequesterId(),
                filter.getDueDateFrom(),
                filter.getDueDateTo(),
                filter.getCreatedFrom(),
                filter.getCreatedTo(),
                filter.getOverdue(),
                filter.getUnassigned(),
                filter.getSearch()
        );

        Sort sort = filter.getSortDirection().equalsIgnoreCase("ASC")
                ? Sort.by(filter.getSortBy()).ascending()
                : Sort.by(filter.getSortBy()).descending();

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);
        Page<WorkOrder> workOrderPage = workOrderRepository.findAll(spec, pageable);

        List<WorkOrderDTO> content = workOrderPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return new PaginatedResponse<>(
                content,
                workOrderPage.getNumber(),
                workOrderPage.getSize(),
                workOrderPage.getTotalElements()
        );
    }

    /**
     * Buscar Work Orders con paginación
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<WorkOrderDTO> searchWorkOrders(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<WorkOrder> workOrderPage = workOrderRepository.searchWorkOrders(search, pageable);

        List<WorkOrderDTO> content = workOrderPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return new PaginatedResponse<>(
                content,
                workOrderPage.getNumber(),
                workOrderPage.getSize(),
                workOrderPage.getTotalElements()
        );
    }

    /**
     * Obtener Work Orders por estado con paginación
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<WorkOrderDTO> getWorkOrdersByStatusPaginated(
            WorkOrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<WorkOrder> workOrderPage = workOrderRepository.findAllByStatus(status, pageable); // CAMBIO AQUÍ

        List<WorkOrderDTO> content = workOrderPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return new PaginatedResponse<>(
                content,
                workOrderPage.getNumber(),
                workOrderPage.getSize(),
                workOrderPage.getTotalElements()
        );
    }

    /**
     * Obtener Work Orders por técnico con paginación
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<WorkOrderDTO> getWorkOrdersByTechnicianPaginated(
            Long techId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<WorkOrder> workOrderPage = workOrderRepository.findAllByAssignedTechId(techId, pageable); // CAMBIO AQUÍ

        List<WorkOrderDTO> content = workOrderPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return new PaginatedResponse<>(
                content,
                workOrderPage.getNumber(),
                workOrderPage.getSize(),
                workOrderPage.getTotalElements()
        );
    }

    public WorkOrderDTO createWorkOrder(WorkOrderCreateRequest request, Long requesterId) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + requesterId));

        WorkOrder workOrder = new WorkOrder();
        workOrder.setCode(generateWorkOrderCode());
        workOrder.setTitle(request.getTitle());
        workOrder.setDescription(request.getDescription());
        workOrder.setPriority(request.getPriority());
        workOrder.setStatus(WorkOrderStatus.OPEN);
        workOrder.setDueDate(request.getDueDate());
        workOrder.setScheduledStart(request.getScheduledStart());
        workOrder.setScheduledEnd(request.getScheduledEnd());
        workOrder.setEstimatedHours(request.getEstimatedHours());
        workOrder.setRequester(requester);
        workOrder.setCreatedBy(requester);

        // Asignar relaciones opcionales
        if (request.getCompanyId() != null) {
            Company company = companyRepository.findById(request.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));
            workOrder.setCompany(company);
        }

        if (request.getSiteId() != null) {
            Site site = siteRepository.findById(request.getSiteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sitio no encontrado"));
            workOrder.setSite(site);
        }

        if (request.getAssetId() != null) {
            Asset asset = assetRepository.findById(request.getAssetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Activo no encontrado"));
            workOrder.setAsset(asset);
        }

        if (request.getAssignedTechId() != null) {
            Technician tech = technicianRepository.findById(request.getAssignedTechId())
                    .orElseThrow(() -> new ResourceNotFoundException("Técnico no encontrado"));
            workOrder.setAssignedTech(tech);
        }

        WorkOrder savedWorkOrder = workOrderRepository.save(workOrder);
        return mapToDTO(savedWorkOrder);
    }

    public WorkOrderDTO assignTechnician(Long workOrderId, Long technicianId, Long assignedBy) {
        WorkOrder workOrder = getWorkOrderEntity(workOrderId);
        Technician technician = technicianRepository.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("Técnico no encontrado con ID: " + technicianId));

        if (!technician.getIsActive()) {
            throw new IllegalStateException("El técnico no está activo");
        }

        workOrder.setAssignedTech(technician);

        User updater = userRepository.findById(assignedBy)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        workOrder.setUpdatedBy(updater);

        WorkOrder updated = workOrderRepository.save(workOrder);
        return mapToDTO(updated);
    }

    public WorkOrderDTO updateStatus(Long workOrderId, WorkOrderUpdateStatusRequest request, Long userId) {
        WorkOrder workOrder = getWorkOrderEntity(workOrderId);
        WorkOrderStatus oldStatus = workOrder.getStatus();
        WorkOrderStatus newStatus = request.getStatus();

        // Validar transiciones de estado
        validateStatusTransition(oldStatus, newStatus);

        workOrder.setStatus(newStatus);

        // Actualizar timestamps según el estado
        LocalDateTime now = LocalDateTime.now();

        if (newStatus == WorkOrderStatus.IN_PROGRESS && workOrder.getStartedAt() == null) {
            workOrder.setStartedAt(now);
        }

        if (newStatus == WorkOrderStatus.COMPLETED) {
            workOrder.setCompletedAt(now);

            if (request.getActualHours() != null) {
                workOrder.setActualHours(request.getActualHours());
            } else if (workOrder.getStartedAt() != null) {
                // Calcular horas automáticamente
                long hours = ChronoUnit.HOURS.between(workOrder.getStartedAt(), now);
                workOrder.setActualHours(BigDecimal.valueOf(hours));
            }
        }

        User updater = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        workOrder.setUpdatedBy(updater);

        WorkOrder updated = workOrderRepository.save(workOrder);
        return mapToDTO(updated);
    }

    @Transactional(readOnly = true)
    public WorkOrderDTO getWorkOrderById(Long id) {
        WorkOrder workOrder = getWorkOrderEntity(id);
        return mapToDTO(workOrder);
    }

    @Transactional(readOnly = true)
    public List<WorkOrderDTO> getAllWorkOrders() {
        return workOrderRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkOrderDTO> getWorkOrdersByStatus(WorkOrderStatus status) {
        return workOrderRepository.findByStatus(status).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkOrderDTO> getWorkOrdersByTechnician(Long techId) {
        return workOrderRepository.findByAssignedTechId(techId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkOrderDTO> getWorkOrdersByAsset(Long assetId) {
        return workOrderRepository.findByAssetId(assetId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkOrderDTO> getOverdueWorkOrders() {
        return workOrderRepository.findOverdueWorkOrders(LocalDate.now()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public WorkOrderDTO updateWorkOrder(Long id, WorkOrderCreateRequest request, Long userId) {
        WorkOrder workOrder = getWorkOrderEntity(id);

        workOrder.setTitle(request.getTitle());
        workOrder.setDescription(request.getDescription());
        workOrder.setPriority(request.getPriority());
        workOrder.setDueDate(request.getDueDate());
        workOrder.setScheduledStart(request.getScheduledStart());
        workOrder.setScheduledEnd(request.getScheduledEnd());
        workOrder.setEstimatedHours(request.getEstimatedHours());

        User updater = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        workOrder.setUpdatedBy(updater);

        WorkOrder updated = workOrderRepository.save(workOrder);
        return mapToDTO(updated);
    }

    public void deleteWorkOrder(Long id) {
        WorkOrder workOrder = getWorkOrderEntity(id);
        workOrder.setStatus(WorkOrderStatus.CANCELLED);
        workOrderRepository.save(workOrder);
    }

    // Métodos auxiliares

    private WorkOrder getWorkOrderEntity(Long id) {
        return workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden de trabajo no encontrada con ID: " + id));
    }

    private String generateWorkOrderCode() {
        long count = workOrderRepository.count();
        return String.format("WO-%05d", count + 1);
    }

    private void validateStatusTransition(WorkOrderStatus from, WorkOrderStatus to) {
        // OPEN puede ir a IN_PROGRESS o CANCELLED
        if (from == WorkOrderStatus.OPEN &&
                to != WorkOrderStatus.IN_PROGRESS &&
                to != WorkOrderStatus.CANCELLED) {
            throw new IllegalStateException("Estado inválido: OPEN solo puede cambiar a IN_PROGRESS o CANCELLED");
        }

        // IN_PROGRESS puede ir a COMPLETED o CANCELLED
        if (from == WorkOrderStatus.IN_PROGRESS &&
                to != WorkOrderStatus.COMPLETED &&
                to != WorkOrderStatus.CANCELLED) {
            throw new IllegalStateException("Estado inválido: IN_PROGRESS solo puede cambiar a COMPLETED o CANCELLED");
        }

        // COMPLETED y CANCELLED son estados finales
        if (from == WorkOrderStatus.COMPLETED || from == WorkOrderStatus.CANCELLED) {
            throw new IllegalStateException("No se puede cambiar el estado de una orden " + from);
        }
    }

    private WorkOrderDTO mapToDTO(WorkOrder wo) {
        WorkOrderDTO dto = new WorkOrderDTO();
        dto.setId(wo.getId());
        dto.setCode(wo.getCode());
        dto.setTitle(wo.getTitle());
        dto.setDescription(wo.getDescription());
        dto.setStatus(wo.getStatus());
        dto.setPriority(wo.getPriority());
        dto.setDueDate(wo.getDueDate());
        dto.setScheduledStart(wo.getScheduledStart());
        dto.setScheduledEnd(wo.getScheduledEnd());
        dto.setStartedAt(wo.getStartedAt());
        dto.setCompletedAt(wo.getCompletedAt());
        dto.setEstimatedHours(wo.getEstimatedHours());
        dto.setActualHours(wo.getActualHours());
        dto.setCreatedAt(wo.getCreatedAt());
        dto.setUpdatedAt(wo.getUpdatedAt());

        if (wo.getCompany() != null) {
            dto.setCompanyId(wo.getCompany().getId());
            dto.setCompanyName(wo.getCompany().getName());
        }

        if (wo.getSite() != null) {
            dto.setSiteId(wo.getSite().getId());
            dto.setSiteName(wo.getSite().getName());
        }

        if (wo.getAsset() != null) {
            dto.setAssetId(wo.getAsset().getId());
            dto.setAssetCode(wo.getAsset().getCode());
            dto.setAssetName(wo.getAsset().getName());
        }

        if (wo.getRequester() != null) {
            dto.setRequesterId(wo.getRequester().getId());
            dto.setRequesterName(wo.getRequester().getFirstName() + " " + wo.getRequester().getLastName());
            dto.setRequesterEmail(wo.getRequester().getEmail());
        }

        if (wo.getAssignedTech() != null) {
            dto.setAssignedTechId(wo.getAssignedTech().getId());
            User techUser = wo.getAssignedTech().getUser();
            dto.setAssignedTechName(techUser.getFirstName() + " " + techUser.getLastName());
        }

        if (wo.getCreatedBy() != null) {
            dto.setCreatedById(wo.getCreatedBy().getId());
            dto.setCreatedByName(wo.getCreatedBy().getFirstName() + " " + wo.getCreatedBy().getLastName());
        }

        // Calcular si está vencida
        if (wo.getDueDate() != null &&
                wo.getStatus() != WorkOrderStatus.COMPLETED &&
                wo.getStatus() != WorkOrderStatus.CANCELLED) {
            dto.setIsOverdue(wo.getDueDate().isBefore(LocalDate.now()));
        }

        // Calcular días abierta
        if (wo.getCreatedAt() != null) {
            long days = ChronoUnit.DAYS.between(wo.getCreatedAt().toLocalDate(), LocalDate.now());
            dto.setDaysOpen(days);
        }

        // Calcular costos estimados/reales
        if (wo.getAssignedTech() != null && wo.getAssignedTech().getHourlyRate() != null) {
            BigDecimal rate = wo.getAssignedTech().getHourlyRate();

            if (wo.getEstimatedHours() != null) {
                dto.setEstimatedCost(wo.getEstimatedHours().multiply(rate));
            }

            if (wo.getActualHours() != null) {
                dto.setActualCost(wo.getActualHours().multiply(rate));
            }
        }

        return dto;
    }
}
