package com.dsu.hello_server;

// Answer of POST /api/auth/login and /api/auth/refresh.
// expiresIn is the access token's lifetime in seconds. The refresh token is a random string,
// not a JWT: the server keeps its hash in refresh_token and deletes it on logout.
public record TokenResponse(String accessToken, String tokenType, long expiresIn, String refreshToken) {
}
