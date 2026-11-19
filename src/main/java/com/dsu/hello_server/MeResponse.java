package com.dsu.hello_server;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

// GET /api/auth/me: who the server thinks is calling, and with which authorities.
public record MeResponse(String username, List<String> authorities) {

    public static MeResponse from(Authentication authentication) {
        return new MeResponse(
                authentication.getName(), // Basic: the username. Token: the sub claim.
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList());
    }
}
