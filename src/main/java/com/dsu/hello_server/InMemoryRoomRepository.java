package com.dsu.hello_server;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

@Repository
public class InMemoryRoomRepository implements RoomRepository {
    private final List<Room> rooms = new ArrayList<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public InMemoryRoomRepository() {
        rooms.add(new Room(nextId.getAndIncrement(), "Room A", 10));
        rooms.add(new Room(nextId.getAndIncrement(), "Room B", 20));
    }

    @Override
    public List<Room> findAll() {
        return List.copyOf(rooms);
    }

    @Override
    public Optional<Room> findById(Long id) {
        for (Room room : rooms) {
            if (room.id().equals(id)) {
                return Optional.of(room);
            }
        }
        return Optional.empty();
    }

    @Override
    public Room save(Room room) {
        if (room.id() == null) {
            Room created = new Room(nextId.getAndIncrement(), room.name(), room.capacity());
            rooms.add(created);
            return created;
        }
        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).id().equals(room.id())) {
                rooms.set(i, room);
                return room;
            }
        }
        rooms.add(room);
        return room;
    }

    @Override
    public boolean deleteById(Long id) {
        return rooms.removeIf(room -> room.id().equals(id));
    }
}
