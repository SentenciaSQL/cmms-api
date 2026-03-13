-- ===========================================
-- V6 - REFRESH TOKENS TABLE
-- ===========================================
-- Los refresh tokens se almacenan en BD para poder revocarlos.
-- Se usa token_hash (SHA-256) en vez del token raw por seguridad.
-- El token raw solo viaja en el response del login/refresh y nunca se persiste.

CREATE TABLE refresh_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(64) NOT NULL UNIQUE,   -- SHA-256 hex del token UUID
    expires_at  TIMESTAMP NOT NULL,
    revoked     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_user    ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_hash    ON refresh_tokens(token_hash);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens(expires_at);

COMMENT ON TABLE refresh_tokens IS 'Refresh tokens para renovar access tokens sin re-autenticar';
COMMENT ON COLUMN refresh_tokens.token_hash IS 'SHA-256 del UUID enviado al cliente. El valor raw nunca se almacena.';
