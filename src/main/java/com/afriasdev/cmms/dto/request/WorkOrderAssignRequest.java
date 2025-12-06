package com.afriasdev.cmms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderAssignRequest {
    @NotNull(message = "El ID del técnico es requerido")
    private Long technicianId;

    private String notes;
}
