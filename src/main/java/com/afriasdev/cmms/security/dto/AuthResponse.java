package com.afriasdev.cmms.security.dto;

import com.afriasdev.cmms.security.model.Role;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private long   expiresAt;   // epoch millis

    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private Set<Role> roles;
}
