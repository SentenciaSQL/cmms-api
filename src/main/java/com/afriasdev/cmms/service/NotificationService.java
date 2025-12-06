package com.afriasdev.cmms.service;

import com.afriasdev.cmms.dto.NotificationDTO;
import com.afriasdev.cmms.dto.UserDTO;
import com.afriasdev.cmms.dto.request.NotificationCreateRequest;
import com.afriasdev.cmms.exception.ResourceNotFoundException;
import com.afriasdev.cmms.model.Notification;
import com.afriasdev.cmms.repository.NotificationRepository;
import com.afriasdev.cmms.security.model.User;
import com.afriasdev.cmms.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public NotificationDTO create(NotificationCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setType(request.getType());
        notification.setCategory(request.getCategory());
        notification.setLink(request.getLink());
        notification.setRelatedEntityId(request.getRelatedEntityId());

        Notification saved = notificationRepository.save(notification);
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> findByUserId(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> findUnreadByUserId(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Long countUnread(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    @Transactional
    public NotificationDTO markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada"));

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());

        Notification saved = notificationRepository.save(notification);
        return toDTO(saved);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Transactional
    public void delete(Long id) {
        notificationRepository.deleteById(id);
    }

    @Transactional
    public void deleteReadNotifications(Long userId) {
        notificationRepository.deleteReadNotificationsByUserId(userId);
    }

    private NotificationDTO toDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());

        // User
        if (notification.getUser() != null) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(notification.getUser().getId());
            userDTO.setUsername(notification.getUser().getUsername());
            userDTO.setEmail(notification.getUser().getEmail());
            userDTO.setFirstName(notification.getUser().getFirstName());
            userDTO.setLastName(notification.getUser().getLastName());
            userDTO.setPhone(notification.getUser().getPhone());
            userDTO.setRoles(notification.getUser().getRoles());
            dto.setUser(userDTO);
        }

        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setCategory(notification.getCategory());
        dto.setIsRead(notification.getIsRead());
        dto.setLink(notification.getLink());
        dto.setRelatedEntityId(notification.getRelatedEntityId());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setReadAt(notification.getReadAt());

        return dto;
    }
}
