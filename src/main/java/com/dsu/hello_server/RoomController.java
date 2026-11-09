package com.dsu.hello_server;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

// HTTP layer for rooms: paths, request bodies, status codes, headers. No rules, no SQL.
// No try/catch either: errors are exceptions from RoomService, turned into ApiError bodies by
// ApiExceptionHandler. Every success returns RoomResponse, never the Room entity.
// The @Operation/@ApiResponse annotations only feed springdoc (Swagger UI at /swagger-ui.html).
@Tag(name = "Rooms", description = "Study room catalogue")
@RestController              // return values are written as JSON by Jackson
@RequestMapping("/api/rooms") // prefix: method mappings add to it, they don't repeat it
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) { // the single shared RoomService bean
        this.roomService = roomService;
    }

    // GET /api/rooms?minCapacity=&keyword=  -> one endpoint, optional filters (not /rooms/search)
    @Operation(summary = "List rooms, optionally filtered by minimum capacity and by keyword")
    @ApiResponse(responseCode = "200", description = "The rooms that match every filter given")
    @ApiResponse(responseCode = "400", description = "minCapacity is not a number")
    @GetMapping
    public List<RoomResponse> list(
            // required = false: absent -> null. "abc" can't become Integer -> 400 from ApiExceptionHandler
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) String keyword) {
        return roomService.search(minCapacity, keyword).stream()
                .map(RoomResponse::from)
                .toList();
    }

    // GET /api/rooms/{id} -> 200 or 404
    @Operation(summary = "Find one room by id")
    @ApiResponse(responseCode = "200", description = "Found")
    @ApiResponse(responseCode = "404", description = "No such room")
    @GetMapping("/{id}")
    public RoomResponse findOne(@PathVariable Long id) {
        return RoomResponse.from(roomService.findById(id)); // missing -> NotFoundException -> 404
    }

    // POST /api/rooms -> 201 + Location, or 400
    @Operation(summary = "Create a room")
    @ApiResponse(responseCode = "201", description = "Created, with a Location header")
    @ApiResponse(responseCode = "400", description = "Name missing or capacity is not between 1 and 20")
    @PostMapping
    public ResponseEntity<RoomResponse> create(@RequestBody RoomRequest request) { // without @RequestBody: all nulls/0
        Room room = roomService.create(request.name(), request.capacity());
        // created() sets status 201 AND the Location header together
        return ResponseEntity.created(URI.create("/api/rooms/" + room.getId())).body(RoomResponse.from(room));
    }

    // PUT /api/rooms/{id} -> 200 / 400 / 404. Idempotent: sending it twice leaves the same state.
    @Operation(summary = "Update an existing room")
    @ApiResponse(responseCode = "200", description = "Updated")
    @ApiResponse(responseCode = "400", description = "Name missing or capacity is not between 1 and 20")
    @ApiResponse(responseCode = "404", description = "No such room")
    @PutMapping("/{id}")
    public RoomResponse update(@PathVariable Long id, @RequestBody RoomRequest request) {
        return RoomResponse.from(roomService.update(id, request.name(), request.capacity()));
    }

    // DELETE /api/rooms/{id} -> 204 / 404 / 409
    @Operation(summary = "Delete a room")
    @ApiResponse(responseCode = "204", description = "Deleted")
    @ApiResponse(responseCode = "404", description = "No such room")
    @ApiResponse(responseCode = "409", description = "Room has reservations")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roomService.delete(id);
        return ResponseEntity.noContent().build(); // 204: success, no body
    }

    // GET /api/rooms/stats -> {"rooms": 3}. Jackson writes a Map as a JSON object.
    // "/stats" is not taken as an {id}: Spring prefers the literal path over the pattern.
    @Operation(summary = "Count rooms")
    @ApiResponse(responseCode = "200", description = "Room count")
    @GetMapping("/stats")
    public Map<String, Long> stats() {
        return Map.of("rooms", roomService.count());
    }
}
