package com.afriasdev.cmms.security.service;

import com.afriasdev.cmms.security.dto.AuthResponse;
import com.afriasdev.cmms.security.dto.LoginRequest;
import com.afriasdev.cmms.security.dto.RegisterRequest;
import com.afriasdev.cmms.security.jwt.JwtService;
import com.afriasdev.cmms.security.model.Role;
import com.afriasdev.cmms.security.model.User;
import com.afriasdev.cmms.security.repository.UserRepository;
import com.afriasdev.cmms.security.service.RefreshTokenService.RotationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email ya est\u00e1 en uso");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username ya est\u00e1 en uso");
        }

        String roleStr = request.getRole().toUpperCase();
        if (!roleStr.startsWith("ROLE_")) roleStr = "ROLE_" + roleStr;

        Role roleEnum;
        try {
            roleEnum = Role.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("El role '" + request.getRole() + "' no es v\u00e1lido");
        }

        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .active(true)
                .roles(Set.of(roleEnum))
                .build();

        userRepository.save(user);
        return buildAuthResponse(user);
    }

    /**
     * Renueva el access token usando un refresh token v\u00e1lido.
     * El refresh token se rota: el anterior queda revocado y se emite uno nuevo.
     */
    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        RotationResult rotation = refreshTokenService.rotateRefreshToken(rawRefreshToken);
        User user = rotation.user();

        long now = System.currentTimeMillis();
        String newAccessToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(rotation.newRawToken())
                .tokenType("Bearer")
                .expiresAt(now + jwtService.getExpirationMs())
                .refreshExpiresAt(now + jwtService.getRefreshExpirationMs())
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles())
                .build();
    }

    /**
     * Logout: revoca el refresh token indicado.
     * Si refreshToken es null, revoca todas las sesiones del usuario.
     */
    @Transactional
    public void logout(Long userId, String rawRefreshToken) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            refreshTokenService.revoke(rawRefreshToken);
        } else {
            refreshTokenService.revokeAllForUser(userId);
        }
    }

    // -------------------------------------------------------------------------

    private AuthResponse buildAuthResponse(User user) {
        long now = System.currentTimeMillis();
        String accessToken  = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresAt(now + jwtService.getExpirationMs())
                .refreshExpiresAt(now + jwtService.getRefreshExpirationMs())
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles())
                .build();
    }
}
