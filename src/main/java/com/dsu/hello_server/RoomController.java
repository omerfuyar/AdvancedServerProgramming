package com.dsu.hello_server;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final List<Room> rooms = new ArrayList<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public RoomController() {
        rooms.add(new Room(nextId.getAndIncrement(), "Room A", 10));
        rooms.add(new Room(nextId.getAndIncrement(), "Room B", 20));
    }

    @GetMapping
    public List<Room> list() {
        return rooms;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> get(@PathVariable long id) {
        return find(id) == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(find(id));
    }

    @PostMapping
    public ResponseEntity<Room> create(@RequestBody RoomRequest req) {
        Room room = new Room(nextId.getAndIncrement(), req.name(), req.capacity());
        rooms.add(room);
        return ResponseEntity.created(URI.create("/api/rooms/" + room.id())).body(room);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Room> update(@PathVariable long id, @RequestBody RoomRequest req) {
        Room old = find(id);
        if (old == null) {
            return ResponseEntity.notFound().build();
        }
        Room updated = new Room(id, req.name(), req.capacity());
        rooms.set(rooms.indexOf(old), updated);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        Room old = find(id);
        if (old == null) {
            return ResponseEntity.notFound().build();
        }
        rooms.remove(old);
        return ResponseEntity.noContent().build();
    }

    private Room find(long id) {
        for (Room r : rooms) {
            if (r.id() == id) {
                return r;
            }
        }
        return null;
    }
}
