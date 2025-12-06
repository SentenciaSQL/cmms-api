package com.afriasdev.cmms.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianDTO {
    private Long id;

    @NotNull(message = "El ID de usuario es requerido")
    private Long userId;

    // Datos del usuario para mostrar
    private String userName;
    private String userEmail;
    private String userFirstName;
    private String userLastName;
    private String userPhone;

    @Size(max = 50, message = "El nivel de habilidad no puede exceder 50 caracteres")
    private String skillLevel; // junior, semi, senior

    @DecimalMin(value = "0.0", inclusive = false, message = "La tarifa debe ser mayor a 0")
    private BigDecimal hourlyRate;

    @Size(max = 50, message = "El teléfono alternativo no puede exceder 50 caracteres")
    private String phoneAlt;

    @Size(max = 255, message = "Las notas no pueden exceder 255 caracteres")
    private String notes;

    private Boolean isActive;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Métricas para dashboard
    private Integer assignedWorkOrders;
    private Integer completedWorkOrders;
    private BigDecimal totalHoursWorked;
}
