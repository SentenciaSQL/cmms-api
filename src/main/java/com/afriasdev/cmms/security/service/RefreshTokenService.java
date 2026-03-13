package com.afriasdev.cmms.security.service;

import com.afriasdev.cmms.security.model.RefreshToken;
import com.afriasdev.cmms.security.model.User;
import com.afriasdev.cmms.security.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Gestión completa del ciclo de vida de los refresh tokens.
 *
 * Flujo de seguridad:
 *  - El token raw (UUID) solo existe en memoria y viaja al cliente una sola vez.
 *  - En BD se almacena únicamente el SHA-256 del token.
 *  - Cada uso del refresh token lo rota: revoca el anterior y emite uno nuevo.
 *    Esto detecta robo de tokens (si alguien usa un token ya rotado, se revocan todos).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-expiration-ms:604800000}") // 7 días por defecto
    private long refreshExpirationMs;

    // -------------------------------------------------------------------------
    // Crear
    // -------------------------------------------------------------------------

    /**
     * Genera un nuevo refresh token para el usuario.
     * @return el token raw (UUID string) — solo valor que se envía al cliente
     */
    @Transactional
    public String createRefreshToken(User user) {
        String rawToken = UUID.randomUUID().toString();
        String tokenHash = hash(rawToken);

        RefreshToken entity = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusNanos(refreshExpirationMs * 1_000_000L))
                .revoked(false)
                .build();

        refreshTokenRepository.save(entity);
        log.debug("Refresh token creado para usuario '{}'", user.getUsername());
        return rawToken;
    }

    // -------------------------------------------------------------------------
    // Validar y rotar
    // -------------------------------------------------------------------------

    /**
     * Valida el token recibido y retorna el usuario asociado.
     * Rota el token: revoca el actual y crea uno nuevo.
     *
     * @param rawToken  el UUID string recibido del cliente
     * @return par {usuario, nuevoTokenRaw}
     * @throws BadCredentialsException si el token es inválido, expirado o revocado
     */
    @Transactional
    public RotationResult rotateRefreshToken(String rawToken) {
        String tokenHash = hash(rawToken);

        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> {
                    log.warn("Intento de uso de refresh token desconocido");
                    return new BadCredentialsException("Refresh token inválido");
                });

        if (!stored.isValid()) {
            // Token revocado o expirado — posible robo. Revocar todos del usuario.
            if (stored.isRevoked()) {
                log.warn("Refresh token ya revocado para usuario '{}'. Posible robo de token. Revocando todas las sesiones.",
                        stored.getUser().getUsername());
                refreshTokenRepository.revokeAllByUserId(stored.getUser().getId());
            } else {
                log.warn("Refresh token expirado para usuario '{}'", stored.getUser().getUsername());
            }
            throw new BadCredentialsException("Refresh token inválido o expirado");
        }

        // Revocar el token actual (rotación)
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        // Emitir nuevo token
        User user = stored.getUser();
        String newRawToken = createRefreshToken(user);

        log.debug("Refresh token rotado para usuario '{}'", user.getUsername());
        return new RotationResult(user, newRawToken);
    }

    // -------------------------------------------------------------------------
    // Revocar (logout)
    // -------------------------------------------------------------------------

    /**
     * Revoca todas las sesiones activas del usuario (logout completo).
     */
    @Transactional
    public void revokeAllForUser(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
        log.debug("Todas las sesiones revocadas para userId={}", userId);
    }

    /**
     * Revoca un refresh token específico (logout de sesión actual).
     */
    @Transactional
    public void revoke(String rawToken) {
        String tokenHash = hash(rawToken);
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    // -------------------------------------------------------------------------
    // Limpieza automática
    // -------------------------------------------------------------------------

    /**
     * Limpia tokens expirados y revocados de la BD.
     * Corre cada día a las 3:00 AM (configurable).
     */
    @Scheduled(cron = "${app.scheduler.refresh-token-cleanup-cron:0 0 3 * * *}")
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime cutoff = LocalDateTime.now();
        refreshTokenRepository.deleteExpiredOrRevoked(cutoff);
        log.info("[Scheduler] Limpieza de refresh tokens completada.");
    }

    // -------------------------------------------------------------------------
    // Utilidades
    // -------------------------------------------------------------------------

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    /** Resultado de una rotación: usuario + nuevo token raw */
    public record RotationResult(User user, String newRawToken) {}
}
