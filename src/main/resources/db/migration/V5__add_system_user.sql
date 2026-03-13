-- ===========================================
-- V5 - SYSTEM USER FOR AUTOMATED SCHEDULER
-- ===========================================
-- Este usuario es usado por el MaintenanceSchedulerService
-- como "creador" de las Órdenes de Trabajo generadas automáticamente.
-- No debe usarse para login normal; es un actor del sistema.

INSERT INTO users (email, username, password, first_name, last_name, is_active)
VALUES (
    'system@cmms.internal',
    'system',
    -- Contraseña deshabilitada (hash inválido - nunca autenticará via login)
    '$2a$10$DISABLED_SYSTEM_USER_CANNOT_LOGIN_XXXXXXXXXXXXXXXXXXXXXXX',
    'Sistema',
    'CMMS',
    true
)
ON CONFLICT (username) DO NOTHING;

-- El usuario system no necesita roles (nunca pasa por @PreAuthorize)
-- pero si tu SecurityConfig lo requiere, añade ROLE_ADMIN:
-- INSERT INTO user_roles (user_id, role)
-- VALUES ((SELECT id FROM users WHERE username = 'system'), 'ROLE_ADMIN')
-- ON CONFLICT DO NOTHING;
