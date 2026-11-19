package com.dsu.hello_server;

// DTO for POST /api/auth/signup. No role field: a client never chooses its own role.
public record SignupRequest(String username, String password) {
}
