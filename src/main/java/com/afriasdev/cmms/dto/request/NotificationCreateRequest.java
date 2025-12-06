package com.afriasdev.cmms.dto.request;

import com.afriasdev.cmms.model.Notification;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCreateRequest {

    @NotNull(message = "El usuario es requerido")
    private Long userId;

    @NotBlank(message = "El título es requerido")
    private String title;

    @NotBlank(message = "El mensaje es requerido")
    private String message;

    @NotNull(message = "El tipo es requerido")
    private Notification.NotificationType type;

    @NotNull(message = "La categoría es requerida")
    private Notification.NotificationCategory category;

    private String link;
    private Long relatedEntityId;
}
