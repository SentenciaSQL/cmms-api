package com.afriasdev.cmms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Habilita el procesamiento de @Scheduled en toda la aplicación.
 * El scheduler corre en un thread pool separado para no bloquear requests HTTP.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
