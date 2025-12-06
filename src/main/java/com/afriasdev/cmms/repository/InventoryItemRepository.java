package com.afriasdev.cmms.repository;

import com.afriasdev.cmms.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    Optional<InventoryItem> findByCode(String code);

    List<InventoryItem> findByIsActiveTrue();

    List<InventoryItem> findByItemType(InventoryItem.ItemType itemType);

    List<InventoryItem> findBySupplierId(Long supplierId);

    @Query("SELECT i FROM InventoryItem i WHERE i.currentStock <= i.minStock AND i.isActive = true")
    List<InventoryItem> findLowStockItems();

    @Query("SELECT i FROM InventoryItem i WHERE i.currentStock <= i.reorderPoint AND i.isActive = true")
    List<InventoryItem> findItemsNeedingReorder();

    @Query("SELECT i FROM InventoryItem i WHERE " +
            "LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(i.code) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(i.partNumber) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<InventoryItem> searchItems(@Param("query") String query);

    @Query("SELECT i FROM InventoryItem i WHERE i.location = :location AND i.isActive = true")
    List<InventoryItem> findByLocation(@Param("location") String location);
}

