package com.dsu.hello_server;

// What the API sends back for an account. No password, no hash.
public record UserResponse(Long id, String username, String role) {

    public static UserResponse from(AppUser user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRole());
    }
}
