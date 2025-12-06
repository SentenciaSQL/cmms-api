package com.afriasdev.cmms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianSummaryDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String skillLevel;
    private BigDecimal hourlyRate;
    private Boolean isActive;

    private Integer assignedWorkOrders;
    private Boolean isAvailable;
}
