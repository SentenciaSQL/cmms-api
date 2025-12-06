package com.afriasdev.cmms.security.service;

import com.afriasdev.cmms.security.dto.AuthResponse;
import com.afriasdev.cmms.security.dto.LoginRequest;
import com.afriasdev.cmms.security.dto.RegisterRequest;
import com.afriasdev.cmms.security.jwt.JwtService;
import com.afriasdev.cmms.security.model.Role;
import com.afriasdev.cmms.security.model.User;
import com.afriasdev.cmms.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );

        String usernameOrEmail = request.getUsernameOrEmail();

        User user = userRepository.findByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtService.generateToken(user);
        long expiresAt = System.currentTimeMillis() + jwtService.getExpirationMs();

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresAt(expiresAt)
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles())
                .build();
    }

    // Método opcional para registrar usuarios en nuevos proyectos
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email ya está en uso");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username ya está en uso");
        }

        String roleStr = request.getRole().toUpperCase();

        // Permitir variantes como: "ADMIN" → "ROLE_ADMIN"
        if (!roleStr.startsWith("ROLE_")) {
            roleStr = "ROLE_" + roleStr;
        }

        Role roleEnum;
        try {
            roleEnum = Role.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("El role '" + request.getRole() + "' no es válido");
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

        String token = jwtService.generateToken(user);
        long expiresAt = System.currentTimeMillis() + jwtService.getExpirationMs();

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresAt(expiresAt)
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles())
                .build();

    }
}
