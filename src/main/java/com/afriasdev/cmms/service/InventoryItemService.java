package com.afriasdev.cmms.service;

import com.afriasdev.cmms.dto.InventoryItemDTO;
import com.afriasdev.cmms.dto.SupplierDTO;
import com.afriasdev.cmms.dto.request.InventoryItemCreateRequest;
import com.afriasdev.cmms.dto.request.StockAdjustmentRequest;
import com.afriasdev.cmms.exception.ResourceNotFoundException;
import com.afriasdev.cmms.model.InventoryItem;
import com.afriasdev.cmms.model.InventoryMovement;
import com.afriasdev.cmms.model.Supplier;
import com.afriasdev.cmms.repository.InventoryItemRepository;
import com.afriasdev.cmms.repository.InventoryMovementRepository;
import com.afriasdev.cmms.repository.SupplierRepository;
import com.afriasdev.cmms.security.model.User;
import com.afriasdev.cmms.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryItemService {

    private final InventoryItemRepository inventoryItemRepository;
    private final SupplierRepository supplierRepository;
    private final InventoryMovementRepository movementRepository;
    private final UserRepository userRepository;

    @Transactional
    public InventoryItemDTO create(InventoryItemCreateRequest request) {
        InventoryItem item = new InventoryItem();
        item.setCode(request.getCode());
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setItemType(request.getItemType());
        item.setCurrentStock(request.getCurrentStock());
        item.setMinStock(request.getMinStock());
        item.setMaxStock(request.getMaxStock());
        item.setReorderPoint(request.getReorderPoint());
        item.setUnit(request.getUnit());
        item.setUnitCost(request.getUnitCost());
        item.setLocation(request.getLocation());
        item.setManufacturer(request.getManufacturer());
        item.setPartNumber(request.getPartNumber());
        item.setImageUrl(request.getImageUrl());

        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));
            item.setSupplier(supplier);
        }

        InventoryItem saved = inventoryItemRepository.save(item);
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<InventoryItemDTO> findAll() {
        return inventoryItemRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InventoryItemDTO findById(Long id) {
        InventoryItem item = inventoryItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ítem no encontrado"));
        return toDTO(item);
    }

    @Transactional(readOnly = true)
    public List<InventoryItemDTO> findLowStock() {
        return inventoryItemRepository.findLowStockItems().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public InventoryItemDTO adjustStock(Long id, StockAdjustmentRequest request, Long userId) {
        InventoryItem item = inventoryItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ítem no encontrado"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        int previousStock = item.getCurrentStock();
        int newStock = previousStock + request.getQuantity();

        if (newStock < 0) {
            throw new IllegalArgumentException("Stock insuficiente");
        }

        item.setCurrentStock(newStock);
        InventoryItem saved = inventoryItemRepository.save(item);

        // Registrar movimiento
        InventoryMovement movement = new InventoryMovement();
        movement.setInventoryItem(item);
        movement.setMovementType(request.getQuantity() > 0 ?
                InventoryMovement.MovementType.IN : InventoryMovement.MovementType.OUT);
        movement.setQuantity(Math.abs(request.getQuantity()));
        movement.setPreviousStock(previousStock);
        movement.setNewStock(newStock);
        movement.setUser(user);
        movement.setNotes(request.getNotes());
        movement.setMovementDate(java.time.LocalDateTime.now());

        movementRepository.save(movement);

        return toDTO(saved);
    }

    @Transactional
    public InventoryItemDTO update(Long id, InventoryItemCreateRequest request) {
        InventoryItem item = inventoryItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ítem no encontrado"));

        item.setCode(request.getCode());
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setItemType(request.getItemType());
        item.setMinStock(request.getMinStock());
        item.setMaxStock(request.getMaxStock());
        item.setReorderPoint(request.getReorderPoint());
        item.setUnit(request.getUnit());
        item.setUnitCost(request.getUnitCost());
        item.setLocation(request.getLocation());
        item.setManufacturer(request.getManufacturer());
        item.setPartNumber(request.getPartNumber());
        item.setImageUrl(request.getImageUrl());

        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));
            item.setSupplier(supplier);
        }

        InventoryItem saved = inventoryItemRepository.save(item);
        return toDTO(saved);
    }

    @Transactional
    public void delete(Long id) {
        InventoryItem item = inventoryItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ítem no encontrado"));
        item.setIsActive(false);
        inventoryItemRepository.save(item);
    }

    private InventoryItemDTO toDTO(InventoryItem item) {
        InventoryItemDTO dto = new InventoryItemDTO();
        dto.setId(item.getId());
        dto.setCode(item.getCode());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setItemType(item.getItemType());

        // Supplier
        if (item.getSupplier() != null) {
            SupplierDTO supplierDTO = new SupplierDTO();
            supplierDTO.setId(item.getSupplier().getId());
            supplierDTO.setName(item.getSupplier().getName());
            supplierDTO.setEmail(item.getSupplier().getEmail());
            supplierDTO.setPhone(item.getSupplier().getPhone());
            dto.setSupplier(supplierDTO);
        }

        dto.setCurrentStock(item.getCurrentStock());
        dto.setMinStock(item.getMinStock());
        dto.setMaxStock(item.getMaxStock());
        dto.setReorderPoint(item.getReorderPoint());
        dto.setUnit(item.getUnit());
        dto.setUnitCost(item.getUnitCost());
        dto.setLocation(item.getLocation());
        dto.setManufacturer(item.getManufacturer());
        dto.setPartNumber(item.getPartNumber());
        dto.setImageUrl(item.getImageUrl());
        dto.setIsActive(item.getIsActive());
        dto.setIsLowStock(item.isLowStock());
        dto.setNeedsReorder(item.needsReorder());
        dto.setCreatedAt(item.getCreatedAt());
        dto.setUpdatedAt(item.getUpdatedAt());

        return dto;
    }
}