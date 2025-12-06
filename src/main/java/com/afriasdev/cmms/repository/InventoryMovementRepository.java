package com.afriasdev.cmms.repository;

import com.afriasdev.cmms.model.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    List<InventoryMovement> findByInventoryItemId(Long inventoryItemId);

    List<InventoryMovement> findByWorkOrderId(Long workOrderId);

    List<InventoryMovement> findByUserId(Long userId);

    List<InventoryMovement> findByMovementType(InventoryMovement.MovementType movementType);

    @Query("SELECT im FROM InventoryMovement im WHERE im.movementDate BETWEEN :startDate AND :endDate")
    List<InventoryMovement> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT im FROM InventoryMovement im WHERE im.inventoryItem.id = :itemId " +
            "AND im.movementDate BETWEEN :startDate AND :endDate")
    List<InventoryMovement> findByItemAndDateRange(@Param("itemId") Long itemId,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT im FROM InventoryMovement im WHERE im.inventoryItem.id = :itemId " +
            "ORDER BY im.movementDate DESC")
    List<InventoryMovement> findByInventoryItemIdOrderByMovementDateDesc(@Param("itemId") Long itemId);
}

