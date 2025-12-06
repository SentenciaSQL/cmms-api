package com.afriasdev.cmms.dto.request;

import jakarta.validation.constraints.NotNull;
import com.afriasdev.cmms.model.WorkOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderUpdateStatusRequest {

    @NotNull(message = "El estado es requerido")
    private WorkOrderStatus status;

    private BigDecimal actualHours;

    private String notes;
}