package com.afriasdev.cmms.dto;

import com.afriasdev.cmms.model.EvidenceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderEvidenceDTO {

    private Long id;

    @NotNull(message = "El ID de la orden de trabajo es requerido")
    private Long workOrderId;

    @NotNull(message = "El tipo de evidencia es requerido")
    private EvidenceType type;

    @NotBlank(message = "La URL es requerida")
    @Size(max = 500, message = "La URL no puede exceder 500 caracteres")
    private String url;

    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String description;

    private LocalDateTime uploadedAt;

    private Long uploadedById;
    private String uploadedByName;
}