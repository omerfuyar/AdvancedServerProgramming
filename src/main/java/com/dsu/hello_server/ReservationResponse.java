package com.dsu.hello_server;

import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,
        Long roomId,
        String roomName,
        String reservedBy,
        LocalDateTime startTime,
        LocalDateTime endTime) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getRoom().getId(),
                reservation.getRoom().getName(),
                reservation.getReservedBy(),
                reservation.getStartTime(),
                reservation.getEndTime());
    }
}
