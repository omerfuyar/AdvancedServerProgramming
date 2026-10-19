package com.dsu.hello_server;

import java.time.LocalDateTime;

// DTO for POST /api/rooms/{roomId}/reservations.
// No id (the database assigns it) and no roomId (it comes from the URL path).
// Jackson parses ISO-8601 strings like "2026-10-14T10:00:00" into LocalDateTime;
// a badly formatted date is rejected with 400 before our code runs.
public record ReservationCreateRequest(
        String reservedBy,
        LocalDateTime startTime,
        LocalDateTime endTime) {
}
