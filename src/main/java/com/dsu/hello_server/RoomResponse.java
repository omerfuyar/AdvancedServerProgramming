package com.dsu.hello_server;

// What the API sends back for a room. Like ReservationResponse: the entity never leaves the server.
// Room has no lazy fields today, but returning it would still make every future column part of
// the API. This record changes only when we decide.
public record RoomResponse(Long id, String name, int capacity) {

    // Static factory: entity -> response. Used as RoomResponse::from in streams.
    public static RoomResponse from(Room room) {
        return new RoomResponse(room.getId(), room.getName(), room.getCapacity());
    }
}
