package com.afriasdev.cmms.service;

import com.afriasdev.cmms.dto.WorkOrderEvidenceDTO;
import com.afriasdev.cmms.dto.request.WorkOrderEvidenceCreateRequest;
import com.afriasdev.cmms.exception.ResourceNotFoundException;
import com.afriasdev.cmms.model.EvidenceType;
import com.afriasdev.cmms.model.WorkOrder;
import com.afriasdev.cmms.model.WorkOrderEvidence;
import com.afriasdev.cmms.repository.WorkOrderEvidenceRepository;
import com.afriasdev.cmms.repository.WorkOrderRepository;
import com.afriasdev.cmms.security.model.User;
import com.afriasdev.cmms.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkOrderEvidenceService {

    private final WorkOrderEvidenceRepository evidenceRepository;
    private final WorkOrderRepository workOrderRepository;
    private final UserRepository userRepository;

    public WorkOrderEvidenceDTO createEvidence(WorkOrderEvidenceCreateRequest request, Long uploadedBy) {
        WorkOrder workOrder = workOrderRepository.findById(request.getWorkOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Orden de trabajo no encontrada con ID: " + request.getWorkOrderId()));

        User uploader = userRepository.findById(uploadedBy)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + uploadedBy));

        WorkOrderEvidence evidence = new WorkOrderEvidence();
        evidence.setWorkOrder(workOrder);
        evidence.setType(request.getType());
        evidence.setUrl(request.getUrl());
        evidence.setDescription(request.getDescription());
        evidence.setUploadedBy(uploader);

        WorkOrderEvidence savedEvidence = evidenceRepository.save(evidence);
        return mapToDTO(savedEvidence);
    }

    @Transactional(readOnly = true)
    public WorkOrderEvidenceDTO getEvidenceById(Long id) {
        WorkOrderEvidence evidence = evidenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evidencia no encontrada con ID: " + id));
        return mapToDTO(evidence);
    }

    @Transactional(readOnly = true)
    public List<WorkOrderEvidenceDTO> getAllEvidences() {
        return evidenceRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkOrderEvidenceDTO> getEvidencesByWorkOrder(Long workOrderId) {
        return evidenceRepository.findByWorkOrderId(workOrderId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkOrderEvidenceDTO> getEvidencesByType(EvidenceType type) {
        return evidenceRepository.findByType(type).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkOrderEvidenceDTO> getEvidencesByWorkOrderAndType(Long workOrderId, EvidenceType type) {
        return evidenceRepository.findByWorkOrderIdAndType(workOrderId, type).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public WorkOrderEvidenceDTO updateEvidence(Long id, WorkOrderEvidenceCreateRequest request) {
        WorkOrderEvidence evidence = evidenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evidencia no encontrada con ID: " + id));

        evidence.setType(request.getType());
        evidence.setUrl(request.getUrl());
        evidence.setDescription(request.getDescription());

        WorkOrderEvidence updatedEvidence = evidenceRepository.save(evidence);
        return mapToDTO(updatedEvidence);
    }

    public void deleteEvidence(Long id) {
        WorkOrderEvidence evidence = evidenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evidencia no encontrada con ID: " + id));

        evidenceRepository.delete(evidence);
    }

    public void deleteEvidencesByWorkOrder(Long workOrderId) {
        List<WorkOrderEvidence> evidences = evidenceRepository.findByWorkOrderId(workOrderId);
        evidenceRepository.deleteAll(evidences);
    }

    @Transactional(readOnly = true)
    public Long countEvidencesByWorkOrder(Long workOrderId) {
        return (long) evidenceRepository.findByWorkOrderId(workOrderId).size();
    }

    @Transactional(readOnly = true)
    public Long countPhotosByWorkOrder(Long workOrderId) {
        return (long) evidenceRepository.findByWorkOrderIdAndType(workOrderId, EvidenceType.PHOTO).size();
    }

    @Transactional(readOnly = true)
    public Long countDocumentsByWorkOrder(Long workOrderId) {
        return (long) evidenceRepository.findByWorkOrderIdAndType(workOrderId, EvidenceType.DOCUMENT).size();
    }

    @Transactional(readOnly = true)
    public Long countAudiosByWorkOrder(Long workOrderId) {
        return (long) evidenceRepository.findByWorkOrderIdAndType(workOrderId, EvidenceType.AUDIO).size();
    }

    private WorkOrderEvidenceDTO mapToDTO(WorkOrderEvidence evidence) {
        WorkOrderEvidenceDTO dto = new WorkOrderEvidenceDTO();
        dto.setId(evidence.getId());
        dto.setWorkOrderId(evidence.getWorkOrder().getId());
        dto.setType(evidence.getType());
        dto.setUrl(evidence.getUrl());
        dto.setDescription(evidence.getDescription());
        dto.setUploadedAt(evidence.getUploadedAt());

        if (evidence.getUploadedBy() != null) {
            dto.setUploadedById(evidence.getUploadedBy().getId());
            dto.setUploadedByName(evidence.getUploadedBy().getFirstName() + " " + evidence.getUploadedBy().getLastName());
        }

        return dto;
    }
}