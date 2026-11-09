package com.dsu.hello_server;

// "The thing the URL names does not exist" (a room or a reservation id).
// The service throws it; ApiExceptionHandler turns it into 404 with an ApiError body.
// Replaces the old Optional.empty() -> ResponseEntity.notFound() pattern, which sent a 404 with no body.
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
