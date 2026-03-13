package com.afriasdev.cmms.model;

import com.afriasdev.cmms.model.AuditableEntity;

import com.afriasdev.cmms.security.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000, nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type; // INFO, WARNING, ERROR, SUCCESS

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationCategory category; // WORK_ORDER, MAINTENANCE, INVENTORY, SYSTEM

    @Column(nullable = false)
    private Boolean isRead = false;

    private String link; // URL para navegar cuando se hace clic

    private Long relatedEntityId; // ID de la entidad relacionada (OT, Asset, etc.)

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime readAt;


    public enum NotificationType {
        INFO, WARNING, ERROR, SUCCESS
    }

    public enum NotificationCategory {
        WORK_ORDER,
        MAINTENANCE,
        INVENTORY,
        ASSET,
        SYSTEM,
        USER
    }
}

