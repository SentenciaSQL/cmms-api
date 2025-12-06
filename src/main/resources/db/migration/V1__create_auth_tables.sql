-- ===========================================
-- V1 - AUTH MODULE: USERS & USER_ROLES (ENUM)
-- ===========================================

-- USERS
CREATE TABLE users (
                       id            BIGSERIAL PRIMARY KEY,
                       email         VARCHAR(150) NOT NULL UNIQUE,
                       username      VARCHAR(100) NOT NULL UNIQUE,
                       password      VARCHAR(255) NOT NULL,
                       first_name    VARCHAR(60) NOT NULL,
                       last_name     VARCHAR(60) NOT NULL,
                       phone         VARCHAR(50),
                       is_active     BOOLEAN NOT NULL DEFAULT TRUE,
                       created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
                       updated_at    TIMESTAMP NOT NULL DEFAULT NOW()
);

-- USER_ROLES (ENUM STRING, SIN TABLA ROLES)
CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            role    VARCHAR(50) NOT NULL,
                            PRIMARY KEY (user_id, role)
);
