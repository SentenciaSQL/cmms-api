package com.afriasdev.cmms.service;

import com.afriasdev.cmms.dto.InventoryItemDTO;
import com.afriasdev.cmms.dto.InventoryMovementDTO;
import com.afriasdev.cmms.dto.UserDTO;
import com.afriasdev.cmms.dto.WorkOrderDTO;
import com.afriasdev.cmms.dto.request.InventoryMovementCreateRequest;
import com.afriasdev.cmms.exception.ResourceNotFoundException;
import com.afriasdev.cmms.model.InventoryItem;
import com.afriasdev.cmms.model.InventoryMovement;
import com.afriasdev.cmms.model.WorkOrder;
import com.afriasdev.cmms.repository.InventoryItemRepository;
import com.afriasdev.cmms.repository.InventoryMovementRepository;
import com.afriasdev.cmms.repository.WorkOrderRepository;
import com.afriasdev.cmms.security.model.User;
import com.afriasdev.cmms.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryMovementService {

    private final InventoryMovementRepository movementRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final WorkOrderRepository workOrderRepository;
    private final UserRepository userRepository;

    @Transactional
    public InventoryMovementDTO create(InventoryMovementCreateRequest request) {
        InventoryItem item = inventoryItemRepository.findById(request.getInventoryItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Ítem no encontrado"));

        // Obtener usuario actual
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        int previousStock = item.getCurrentStock();
        int newStock;

        // Calcular nuevo stock
        if (request.getMovementType() == InventoryMovement.MovementType.IN) {
            newStock = previousStock + request.getQuantity();
        } else if (request.getMovementType() == InventoryMovement.MovementType.OUT) {
            if (previousStock < request.getQuantity()) {
                throw new IllegalArgumentException("Stock insuficiente");
            }
            newStock = previousStock - request.getQuantity();
        } else {
            newStock = request.getQuantity(); // ADJUSTMENT
        }

        // Actualizar stock
        item.setCurrentStock(newStock);
        inventoryItemRepository.save(item);

        // Crear movimiento
        InventoryMovement movement = new InventoryMovement();
        movement.setInventoryItem(item);
        movement.setMovementType(request.getMovementType());
        movement.setQuantity(request.getQuantity());
        movement.setPreviousStock(previousStock);
        movement.setNewStock(newStock);
        movement.setUnitCost(request.getUnitCost() != null ? request.getUnitCost() : item.getUnitCost());
        movement.setTotalCost(movement.getUnitCost().multiply(new java.math.BigDecimal(request.getQuantity())));
        movement.setUser(user);
        movement.setNotes(request.getNotes());
        movement.setReferenceNumber(request.getReferenceNumber());
        movement.setMovementDate(request.getMovementDate() != null ? request.getMovementDate() : LocalDateTime.now());

        if (request.getWorkOrderId() != null) {
            WorkOrder workOrder = workOrderRepository.findById(request.getWorkOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Orden de trabajo no encontrada"));
            movement.setWorkOrder(workOrder);
        }

        InventoryMovement saved = movementRepository.save(movement);
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<InventoryMovementDTO> findAll() {
        return movementRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InventoryMovementDTO> findByInventoryItemId(Long itemId) {
        return movementRepository.findByInventoryItemIdOrderByMovementDateDesc(itemId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InventoryMovementDTO> findByWorkOrderId(Long workOrderId) {
        return movementRepository.findByWorkOrderId(workOrderId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InventoryMovementDTO> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return movementRepository.findByDateRange(startDate, endDate).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private InventoryMovementDTO toDTO(InventoryMovement movement) {
        InventoryMovementDTO dto = new InventoryMovementDTO();
        dto.setId(movement.getId());

        // InventoryItem
        if (movement.getInventoryItem() != null) {
            InventoryItemDTO itemDTO = new InventoryItemDTO();
            itemDTO.setId(movement.getInventoryItem().getId());
            itemDTO.setCode(movement.getInventoryItem().getCode());
            itemDTO.setName(movement.getInventoryItem().getName());
            dto.setInventoryItem(itemDTO);
        }

        dto.setMovementType(movement.getMovementType());
        dto.setQuantity(movement.getQuantity());
        dto.setPreviousStock(movement.getPreviousStock());
        dto.setNewStock(movement.getNewStock());
        dto.setUnitCost(movement.getUnitCost());
        dto.setTotalCost(movement.getTotalCost());

        // WorkOrder
        if (movement.getWorkOrder() != null) {
            WorkOrderDTO woDTO = new WorkOrderDTO();
            woDTO.setId(movement.getWorkOrder().getId());
            // Completa con los campos de tu WorkOrder
            dto.setWorkOrder(woDTO);
        }

        // User
        if (movement.getUser() != null) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(movement.getUser().getId());
            userDTO.setUsername(movement.getUser().getUsername());
            userDTO.setEmail(movement.getUser().getEmail());
            userDTO.setFirstName(movement.getUser().getFirstName());
            userDTO.setLastName(movement.getUser().getLastName());
            userDTO.setRoles(movement.getUser().getRoles());
            dto.setUser(userDTO);
        }

        dto.setNotes(movement.getNotes());
        dto.setReferenceNumber(movement.getReferenceNumber());
        dto.setMovementDate(movement.getMovementDate());
        dto.setCreatedAt(movement.getCreatedAt());

        return dto;
    }
}
