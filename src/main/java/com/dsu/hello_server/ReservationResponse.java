package com.dsu.hello_server;

import java.time.LocalDateTime;

// What the API sends back for a reservation. We never return the Reservation entity itself:
//  - its room field is a LAZY Hibernate proxy, and serializing it can fail (hibernateLazyInitializer)
//  - every entity field would become part of the API
//  - this record is OUR contract; it changes only when we decide, not when the table does
public record ReservationResponse(
        Long id,
        Long roomId,
        String roomName,
        String reservedBy,
        LocalDateTime startTime,
        LocalDateTime endTime) {

    // Static factory: entity -> response. Used as ReservationResponse::from in streams.
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getRoom().getId(),   // proxy already knows its id: no SQL
                reservation.getRoom().getName(), // first real access to the proxy: SELECT from room
                                                 // (unless join fetch already loaded it)
                reservation.getReservedBy(),
                reservation.getStartTime(),
                reservation.getEndTime());
    }
}
