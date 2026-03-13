package com.afriasdev.cmms.security.controller;

import com.afriasdev.cmms.security.dto.AuthResponse;
import com.afriasdev.cmms.security.dto.LoginRequest;
import com.afriasdev.cmms.security.dto.LogoutRequest;
import com.afriasdev.cmms.security.dto.RefreshRequest;
import com.afriasdev.cmms.security.dto.RegisterRequest;
import com.afriasdev.cmms.security.service.AuthService;
import com.afriasdev.cmms.security.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Autenticaci\u00f3n y gesti\u00f3n de sesiones")
public class AuthController {

    private final AuthService authService;

    // ------------------------------------------------------------------
    // Login
    // ------------------------------------------------------------------

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesi\u00f3n", description = "Retorna access token (1h) + refresh token (7d)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Autenticado correctamente"),
        @ApiResponse(responseCode = "401", description = "Credenciales inv\u00e1lidas")
    })
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException | UsernameNotFoundException ex) {
            return ResponseEntity.status(401).build();
        }
    }

    // ------------------------------------------------------------------
    // Register
    // ------------------------------------------------------------------

    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    // ------------------------------------------------------------------
    // Refresh
    // ------------------------------------------------------------------

    @PostMapping("/refresh")
    @Operation(
        summary = "Renovar access token",
        description = "Usa el refresh token para obtener un nuevo par access/refresh. El refresh token anterior queda revocado (rotaci\u00f3n)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tokens renovados"),
        @ApiResponse(responseCode = "401", description = "Refresh token inv\u00e1lido o expirado")
    })
    public ResponseEntity<AuthResponse> refresh(@RequestBody @Valid RefreshRequest request) {
        try {
            AuthResponse response = authService.refresh(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).build();
        }
    }

    // ------------------------------------------------------------------
    // Logout
    // ------------------------------------------------------------------

    @PostMapping("/logout")
    @Operation(
        summary = "Cerrar sesi\u00f3n",
        description = "Revoca el refresh token indicado. Si no se env\u00eda refreshToken, se revocan todas las sesiones del usuario."
    )
    @ApiResponse(responseCode = "204", description = "Sesi\u00f3n cerrada")
    public ResponseEntity<Void> logout(@RequestBody(required = false) LogoutRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        String rawToken = (request != null) ? request.getRefreshToken() : null;
        authService.logout(userId, rawToken);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------
    // Status (health check)
    // ------------------------------------------------------------------

    @GetMapping("/status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok("Auth service is running");
    }
}
