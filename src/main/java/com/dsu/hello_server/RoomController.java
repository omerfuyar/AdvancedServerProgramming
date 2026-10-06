package com.dsu.hello_server;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Rooms", description = "Study room catalogue")
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @Operation(summary = "List rooms, optionally filtered by minimum capacity and by keyword")
    @ApiResponse(responseCode = "200", description = "The rooms that match every filter given")
    @GetMapping
    public List<Room> list(
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) String keyword) {
        return roomService.search(minCapacity, keyword);
    }

    @Operation(summary = "Find one room by id")
    @ApiResponse(responseCode = "200", description = "Found")
    @ApiResponse(responseCode = "404", description = "No such room")
    @GetMapping("/{id}")
    public ResponseEntity<Room> findOne(@PathVariable Long id) {
        return roomService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a room")
    @ApiResponse(responseCode = "201", description = "Created, with a Location header")
    @ApiResponse(responseCode = "400", description = "Capacity is not between 1 and 20")
    @PostMapping
    public ResponseEntity<Room> create(@RequestBody RoomRequest request) {
        try {
            Room room = roomService.create(request.name(), request.capacity());
            return ResponseEntity.created(URI.create("/api/rooms/" + room.getId())).body(room);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Replace an existing room")
    @ApiResponse(responseCode = "200", description = "Replaced")
    @ApiResponse(responseCode = "400", description = "Capacity is not between 1 and 20")
    @ApiResponse(responseCode = "404", description = "No such room")
    @PutMapping("/{id}")
    public ResponseEntity<Room> replace(@PathVariable Long id, @RequestBody RoomRequest request) {
        try {
            return roomService.replace(id, request.name(), request.capacity())
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Delete a room")
    @ApiResponse(responseCode = "204", description = "Deleted")
    @ApiResponse(responseCode = "404", description = "No such room")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!roomService.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
