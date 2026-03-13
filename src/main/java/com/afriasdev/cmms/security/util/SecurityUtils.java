package com.afriasdev.cmms.security.util;

import com.afriasdev.cmms.security.details.CustomUserDetails;
import com.afriasdev.cmms.security.model.Role;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utilidades para obtener datos del usuario autenticado desde el SecurityContext.
 * Centraliza la extracción del principal para evitar casting repetido en los controladores.
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    /**
     * Retorna el CustomUserDetails del usuario autenticado actualmente.
     * Lanza AccessDeniedException si no hay autenticación activa.
     */
    public static CustomUserDetails getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            throw new AccessDeniedException("No hay sesión activa");
        }
        return (CustomUserDetails) auth.getPrincipal();
    }

    /**
     * Retorna el ID del usuario autenticado.
     */
    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Retorna el ID del usuario autenticado desde el objeto Authentication inyectado.
     * Útil cuando el controlador ya recibe Authentication como parámetro.
     */
    public static Long getUserIdFromAuth(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new AccessDeniedException("No hay sesión activa");
        }
        return ((CustomUserDetails) authentication.getPrincipal()).getId();
    }

    /**
     * Verifica si el usuario actual tiene un rol específico.
     */
    public static boolean hasRole(Role role) {
        return getCurrentUser().getRoles().contains(role);
    }

    /**
     * Verifica si el usuario actual tiene alguno de los roles indicados.
     */
    public static boolean hasAnyRole(Role... roles) {
        var userRoles = getCurrentUser().getRoles();
        for (Role role : roles) {
            if (userRoles.contains(role)) return true;
        }
        return false;
    }
}
