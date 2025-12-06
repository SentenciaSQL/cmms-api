package com.afriasdev.cmms.dto;

import com.afriasdev.cmms.model.Notification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private Long id;
    private UserDTO user;
    private String title;
    private String message;
    private Notification.NotificationType type;
    private Notification.NotificationCategory category;
    private Boolean isRead;
    private String link;
    private Long relatedEntityId;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
