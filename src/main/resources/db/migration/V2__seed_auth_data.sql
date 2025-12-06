-- ===========================================
-- V2 - SEED INITIAL USERS & ROLES (ENUM)
-- ===========================================

-- Insert users (passwords en BCrypt: reemplaza por los tuyos)
INSERT INTO users (email, username, password, first_name, last_name, phone)
VALUES
    ('admin@cmms.com', 'admin',
     '$2a$10$RNin8zxySUZtxjdru6pSeurgepHI0Th8AylIyknELC91FcA203gvS',
     'Andres', 'Frias', '809-111-1111'),

    ('technician@cmms.com', 'technician',
     '$2a$10$RNin8zxySUZtxjdru6pSeurgepHI0Th8AylIyknELC91FcA203gvS',
     'Technician', 'Java', '809-111-1113'),

    ('manager@cmms.com', 'manager',
     '$2a$10$RNin8zxySUZtxjdru6pSeurgepHI0Th8AylIyknELC91FcA203gvS',
     'Manager', 'General', '809-111-1122'),

    ('user@cmms.com', 'user',
     '$2a$10$lHXrVaUmHA6ZMX2oX0UqSeYmfhgJWUah61Ra874Bhas1obk3uqzEe',
     'Usuario', 'Demo', '809-222-2222');

-- Asignar roles (como strings, iguales al enum Role)
INSERT INTO user_roles (user_id, role)
VALUES
    ((SELECT id FROM users WHERE username = 'admin'), 'ROLE_ADMIN'),
    ((SELECT id FROM users WHERE username = 'admin'), 'ROLE_MANAGER'),
    ((SELECT id FROM users WHERE username = 'manager'), 'ROLE_MANAGER'),
    ((SELECT id FROM users WHERE username = 'technician'),  'ROLE_TECHNICIAN'),
    ((SELECT id FROM users WHERE username = 'user'),  'ROLE_REQUESTER');
