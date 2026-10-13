package com.dsu.hello_server;

import java.time.LocalDateTime;

public record ReservationCreateRequest(
        String reservedBy,
        LocalDateTime startTime,
        LocalDateTime endTime) {
}
