package com.afriasdev.cmms.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {

    private Long id;

    @NotNull(message = "El ID de usuario es requerido")
    private Long userId;

    // Datos del usuario
    private String userName;
    private String userEmail;
    private String userFirstName;
    private String userLastName;
    private String userPhone;

    private Long companyId;
    private String companyName;

    @Size(max = 100, message = "El cargo no puede exceder 100 caracteres")
    private String position;

    @Size(max = 50, message = "El teléfono alternativo no puede exceder 50 caracteres")
    private String phoneAlt;

    @Size(max = 255, message = "Las notas no pueden exceder 255 caracteres")
    private String notes;

    private Boolean isActive;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer requestedWorkOrders;
}
