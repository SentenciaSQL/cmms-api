package com.afriasdev.cmms.repository;

import com.afriasdev.cmms.model.Technician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TechnicianRepository extends JpaRepository<Technician, Long> {
    Optional<Technician> findByUserId(Long userId);
    List<Technician> findByIsActiveTrue();
    List<Technician> findBySkillLevel(String skillLevel);

    @Query("SELECT t FROM Technician t WHERE t.isActive = true " +
            "AND (SELECT COUNT(wo) FROM WorkOrder wo " +
            "WHERE wo.assignedTech.id = t.id AND wo.status IN ('OPEN', 'IN_PROGRESS')) < :maxWorkOrders")
    List<Technician> findAvailableTechnicians(int maxWorkOrders);
}
