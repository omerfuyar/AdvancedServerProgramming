package com.dsu.hello_server;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final InMemoryRoomRepository roomRepository;

    public RoomController(InMemoryRoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @GetMapping
    public List<Room> list() {
        return roomRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> findOne(@PathVariable Long id) {
        return roomRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Room> create(@RequestBody RoomRequest request) {
        Room room = roomRepository.save(new Room(null, request.name(), request.capacity()));
        return ResponseEntity.created(URI.create("/api/rooms/" + room.id())).body(room);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Room> replace(@PathVariable Long id, @RequestBody RoomRequest request) {
        if (roomRepository.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Room updated = roomRepository.save(new Room(id, request.name(), request.capacity()));
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!roomRepository.deleteById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
