package com.afriasdev.cmms.repository;

import com.afriasdev.cmms.model.WorkOrder;
import com.afriasdev.cmms.model.WorkOrderPriority;
import com.afriasdev.cmms.model.WorkOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long>, JpaSpecificationExecutor<WorkOrder> {
    // ========== MÉTODOS SIN PAGINACIÓN ==========
    List<WorkOrder> findByStatus(WorkOrderStatus status);
    List<WorkOrder> findByPriority(WorkOrderPriority priority);
    List<WorkOrder> findByAssignedTechId(Long techId);
    List<WorkOrder> findByRequesterId(Long requesterId);
    List<WorkOrder> findByAssetId(Long assetId);
    List<WorkOrder> findBySiteId(Long siteId);
    List<WorkOrder> findByCompanyId(Long companyId);

    @Query("SELECT wo FROM WorkOrder wo WHERE wo.status IN :statuses")
    List<WorkOrder> findByStatusIn(@Param("statuses") List<WorkOrderStatus> statuses);

    @Query("SELECT wo FROM WorkOrder wo WHERE wo.dueDate < :date AND wo.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<WorkOrder> findOverdueWorkOrders(@Param("date") LocalDate date);

    @Query("SELECT wo FROM WorkOrder wo WHERE wo.createdAt BETWEEN :startDate AND :endDate")
    List<WorkOrder> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(wo) FROM WorkOrder wo WHERE wo.assignedTech.id = :techId AND wo.status IN ('OPEN', 'IN_PROGRESS')")
    Long countActiveWorkOrdersByTechId(@Param("techId") Long techId);

    Long countByStatus(WorkOrderStatus status);
    Long countByPriority(WorkOrderPriority priority);

    // ========== MÉTODOS CON PAGINACIÓN (nombres diferentes) ==========
    Page<WorkOrder> findAllByStatus(WorkOrderStatus status, Pageable pageable);
    Page<WorkOrder> findAllByPriority(WorkOrderPriority priority, Pageable pageable);
    Page<WorkOrder> findAllByAssignedTechId(Long techId, Pageable pageable);
    Page<WorkOrder> findAllByRequesterId(Long requesterId, Pageable pageable);
    Page<WorkOrder> findAllByAssetId(Long assetId, Pageable pageable);
    Page<WorkOrder> findAllBySiteId(Long siteId, Pageable pageable);
    Page<WorkOrder> findAllByCompanyId(Long companyId, Pageable pageable);

    @Query("SELECT wo FROM WorkOrder wo WHERE wo.status IN :statuses")
    Page<WorkOrder> findPageByStatusIn(@Param("statuses") List<WorkOrderStatus> statuses, Pageable pageable);

    @Query("SELECT wo FROM WorkOrder wo WHERE wo.dueDate < :date AND wo.status NOT IN ('COMPLETED', 'CANCELLED')")
    Page<WorkOrder> findPageOfOverdueWorkOrders(@Param("date") LocalDate date, Pageable pageable);

    @Query("SELECT wo FROM WorkOrder wo WHERE wo.createdAt BETWEEN :startDate AND :endDate")
    Page<WorkOrder> findPageByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate,
                                               Pageable pageable);

    @Query("SELECT wo FROM WorkOrder wo WHERE " +
            "LOWER(wo.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(wo.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(wo.code) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<WorkOrder> searchWorkOrders(@Param("search") String search, Pageable pageable);
}
