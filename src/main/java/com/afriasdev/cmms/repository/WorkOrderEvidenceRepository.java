package com.afriasdev.cmms.repository;

import com.afriasdev.cmms.model.EvidenceType;
import com.afriasdev.cmms.model.WorkOrderEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkOrderEvidenceRepository extends JpaRepository<WorkOrderEvidence, Long> {
    List<WorkOrderEvidence> findByWorkOrderId(Long workOrderId);
    List<WorkOrderEvidence> findByType(EvidenceType type);
    List<WorkOrderEvidence> findByWorkOrderIdAndType(Long workOrderId, EvidenceType type);

}
