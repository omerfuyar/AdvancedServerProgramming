package com.dsu.hello_server;

// DTO for POST /api/auth/login
public record LoginRequest(String username, String password) {
}
