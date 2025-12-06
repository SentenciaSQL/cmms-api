package com.afriasdev.cmms.repository;

import com.afriasdev.cmms.model.MaintenancePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MaintenancePlanRepository extends JpaRepository<MaintenancePlan, Long> {

    List<MaintenancePlan> findByIsActiveTrue();

    List<MaintenancePlan> findByAssetId(Long assetId);

    List<MaintenancePlan> findByAssignedTechnicianId(Long technicianId);

    @Query("SELECT mp FROM MaintenancePlan mp WHERE mp.nextScheduledDate <= :date AND mp.isActive = true")
    List<MaintenancePlan> findDueMaintenancePlans(@Param("date") LocalDateTime date);

    @Query("SELECT mp FROM MaintenancePlan mp WHERE mp.autoGenerateWorkOrder = true " +
            "AND mp.nextScheduledDate <= :date AND mp.isActive = true")
    List<MaintenancePlan> findPlansForAutoWorkOrderGeneration(@Param("date") LocalDateTime date);

    List<MaintenancePlan> findByType(MaintenancePlan.MaintenanceType type);

    List<MaintenancePlan> findByPriority(MaintenancePlan.Priority priority);

    @Query("SELECT mp FROM MaintenancePlan mp WHERE mp.asset.site.company.id = :companyId AND mp.isActive = true")
    List<MaintenancePlan> findByCompanyId(@Param("companyId") Long companyId);
}

