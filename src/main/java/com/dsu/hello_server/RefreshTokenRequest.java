package com.dsu.hello_server;

// DTO for POST /api/auth/refresh and /api/auth/logout
public record RefreshTokenRequest(String refreshToken) {
}
