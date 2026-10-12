package com.dsu.hello_server;

public class RoomHasReservationsException extends RuntimeException {

    public RoomHasReservationsException(Long roomId, Throwable cause) {
        super("Room " + roomId + " has reservations", cause);
    }
}
