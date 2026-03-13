package com.afriasdev.cmms.config;

import com.afriasdev.cmms.security.details.CustomUserDetails;
import com.afriasdev.cmms.security.model.User;
import com.afriasdev.cmms.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Implementación de AuditorAware que extrae el User autenticado del SecurityContext.
 *
 * Casos cubiertos:
 *  - Request autenticado con JWT          → retorna el User del token
 *  - Job schedulado (@Scheduled)          → sin auth activa, retorna empty (JPA no rellena los campos by)
 *  - Tests sin contexto de seguridad      → retorna empty de forma segura
 *
 * Nota: el scheduler (MaintenanceSchedulerService) setea explícitamente createdBy
 * en la WorkOrder que crea, por lo que el empty aquí no es un problema.
 */
@Slf4j
@Component("auditorProvider")
@RequiredArgsConstructor
public class AuditorAwareImpl implements AuditorAware<User> {

    private final UserRepository userRepository;

    @Override
    public Optional<User> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof CustomUserDetails details)) {
            return Optional.empty();
        }

        // Cargamos la entidad User completa para el @ManyToOne — evitar referencias detached
        return userRepository.findById(details.getId());
    }
}
