package com.afriasdev.cmms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Activa el sistema de auditoría de Spring Data JPA.
 * auditorAwareRef apunta al bean AuditorAwareImpl que resuelve el usuario actual.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {
}
