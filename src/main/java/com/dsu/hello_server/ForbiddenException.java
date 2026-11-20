package com.dsu.hello_server;

// "We know who you are, and this is not yours" (rule C, ownership).
// The service throws it; ApiExceptionHandler turns it into 403 with an ApiError body.
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
