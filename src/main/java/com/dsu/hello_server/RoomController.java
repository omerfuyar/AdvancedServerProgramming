package com.dsu.hello_server;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public List<Room> list(
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) String keyword) {
        return roomService.search(minCapacity, keyword);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> findOne(@PathVariable Long id) {
        return roomService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Room> create(@RequestBody RoomRequest request) {
        Room room = roomService.create(request.name(), request.capacity());
        return ResponseEntity.created(URI.create("/api/rooms/" + room.id())).body(room);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Room> replace(@PathVariable Long id, @RequestBody RoomRequest request) {
        return roomService.replace(id, request.name(), request.capacity())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!roomService.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
