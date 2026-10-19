package com.dsu.hello_server;

// "The request is valid, but it clashes with data that already exists."
// The service throws it; controllers translate it into HTTP 409 Conflict.
// Extends RuntimeException (unchecked), so no "throws" clauses are needed,
// and an exception like this would also roll back a @Transactional method.
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
