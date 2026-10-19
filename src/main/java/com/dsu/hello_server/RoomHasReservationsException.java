package com.dsu.hello_server;

// Week 6 (Assignment 4) version of the 409: thrown when the database's foreign key refused
// to delete a room that reservations still point to. Wraps the original DB exception as the cause.
// Since Week 7, RoomService checks first and throws ConflictException instead; this one is
// only the fallback if the database still refuses (see RoomService.delete).
public class RoomHasReservationsException extends RuntimeException {

    public RoomHasReservationsException(Long roomId, Throwable cause) {
        super("Room " + roomId + " has reservations", cause);
    }
}
