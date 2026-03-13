package com.afriasdev.cmms.security.dto;

import com.afriasdev.cmms.security.model.Role;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;        // UUID raw — solo viaja en este response
    private String tokenType;
    private long   expiresAt;           // access token expiry (epoch ms)
    private long   refreshExpiresAt;    // refresh token expiry (epoch ms)

    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private Set<Role> roles;
}
