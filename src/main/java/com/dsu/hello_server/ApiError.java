package com.dsu.hello_server;

import java.time.Instant;

import org.springframework.http.HttpStatus;

// The one body every error response has, whatever went wrong:
// {"timestamp": "...", "status": 404, "error": "Not Found", "message": "Room 99 not found", "path": "/api/rooms/99"}
//  - status and error repeat the HTTP status line, so a client reading only the body still knows it
//  - message is for humans: what was wrong with THIS request
//  - path is the URL that failed
// Built only by ApiExceptionHandler; controllers never create it themselves.
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path) {

    public static ApiError of(HttpStatus status, String message, String path) {
        // getReasonPhrase(): 404 -> "Not Found", 409 -> "Conflict"
        return new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, path);
    }
}
