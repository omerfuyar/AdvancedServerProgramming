package com.dsu.hello_server;

import java.time.LocalDateTime;

// DTO for POST /api/rooms/{roomId}/reservations.
// Also the body of PUT /api/reservations/{id}.
// No id (the database assigns it), no roomId (it comes from the URL path) and no owner
// (it comes from the login).
// Jackson parses ISO-8601 strings like "2026-10-14T10:00:00" into LocalDateTime;
// a badly formatted date is rejected with 400 before our code runs.
public record ReservationCreateRequest(
        LocalDateTime startTime,
        LocalDateTime endTime) {
}
