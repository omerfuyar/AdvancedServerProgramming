package com.dsu.hello_server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;

// One place that turns exceptions into HTTP errors, for every controller.
// Controllers no longer need try/catch: a service throws, the exception leaves the controller,
// and Spring calls the matching @ExceptionHandler below (the most specific type wins).
// Every handler returns the same ApiError body, so a client parses errors one way only.
//
//   service throws IllegalArgumentException -> 400  the request itself is wrong
//   Spring cannot read the request          -> 400  bad JSON, "abc" for a number, missing parameter
//   service throws NotFoundException        -> 404  the room/reservation does not exist
//   unknown URL                             -> 404
//   service throws ConflictException        -> 409  valid, but clashes with existing data
//   the database refuses (foreign key)      -> 409  the safety net behind the service checks
//   anything else                           -> 500  a bug on our side
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    // ---------- 400: the request is wrong ----------

    // Our own rules: capacity 1-20, name required, start before end, ...
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> badRequest(IllegalArgumentException e, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, e.getMessage(), request);
    }

    // /api/rooms/abc or ?minCapacity=abc: the text cannot become a Long/Integer
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> typeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST,
                "'" + e.getName() + "' has an invalid value '" + e.getValue() + "'", request);
    }

    // GET /api/reservations without ?reservedBy=
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> missingParameter(MissingServletRequestParameterException e,
            HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "'" + e.getParameterName() + "' is required", request);
    }

    // Broken JSON, or a value Jackson cannot convert ("startTime": "tomorrow")
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> unreadableBody(HttpMessageNotReadableException e, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "Request body is missing or is not valid JSON", request);
    }

    // ---------- 404: it does not exist ----------

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> notFound(NotFoundException e, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, e.getMessage(), request);
    }

    // A URL no controller maps, e.g. /api/nothing
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> noSuchPath(NoResourceFoundException e, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "No endpoint " + request.getMethod() + " " + request.getRequestURI(),
                request);
    }

    // ---------- 405: the path exists, the method does not ----------

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> methodNotAllowed(HttpRequestMethodNotSupportedException e,
            HttpServletRequest request) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, e.getMethod() + " is not supported here", request);
    }

    // ---------- 409: valid, but it clashes with the data ----------

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> conflict(ConflictException e, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, e.getMessage(), request);
    }

    // Strategy A from Week 7: if a service check is ever skipped, the foreign key still says no.
    // The database message names tables and constraints, so it goes to the log, not to the client.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> integrity(DataIntegrityViolationException e, HttpServletRequest request) {
        log.warn("Database refused {} {}", request.getMethod(), request.getRequestURI(), e);
        return error(HttpStatus.CONFLICT, "The request conflicts with existing data", request);
    }

    // ---------- 500: our fault ----------

    // Last resort. The stack trace goes to the console; the client gets a fixed message,
    // never e.getMessage(), which could leak SQL or class names.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unexpected(Exception e, HttpServletRequest request) {
        log.error("Unexpected error on {} {}", request.getMethod(), request.getRequestURI(), e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request);
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(ApiError.of(status, message, request.getRequestURI()));
    }
}
