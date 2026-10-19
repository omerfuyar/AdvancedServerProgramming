package com.dsu.hello_server;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

// HTTP layer only: maps URLs to service calls and service results to status codes.
// URL design: nested (/rooms/{roomId}/reservations) when the room matters (list, create),
// flat (/reservations/{id}) when the reservation's own id is enough (read, cancel).
@Tag(name = "Reservations", description = "Room reservations") // Swagger UI grouping
@RestController            // @Controller + @ResponseBody: return values become the JSON body
@RequestMapping("/api")    // prefix for every mapping in this class
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) { // injected by Spring
        this.reservationService = reservationService;
    }

    // GET /api/rooms/{roomId}/reservations -> 200 with a list, or 404 for a missing room
    @Operation(summary = "List reservations for a room")
    @ApiResponse(responseCode = "200", description = "Reservations ordered by start time")
    @ApiResponse(responseCode = "404", description = "No such room")
    @GetMapping("/rooms/{roomId}/reservations")
    public ResponseEntity<List<ReservationResponse>> listForRoom(@PathVariable Long roomId) {
        // @PathVariable Long: Spring converts "1" -> 1L; "abc" -> 400 before this method runs
        return reservationService.findByRoom(roomId)
                // present: convert each entity to a response record, wrap in 200
                .map(reservations -> ResponseEntity.ok(
                        reservations.stream().map(ReservationResponse::from).toList()))
                // empty: room doesn't exist -> 404 with no body
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST /api/rooms/{roomId}/reservations -> 201 / 400 / 404 / 409
    @Operation(summary = "Create a reservation for a room")
    @ApiResponse(responseCode = "201", description = "Created, with a Location header")
    @ApiResponse(responseCode = "400", description = "Invalid reservation request")
    @ApiResponse(responseCode = "404", description = "No such room")
    @ApiResponse(responseCode = "409", description = "Reservation overlaps an existing reservation")
    @PostMapping("/rooms/{roomId}/reservations")
    public ResponseEntity<ReservationResponse> create(
            @PathVariable Long roomId,
            @RequestBody ReservationCreateRequest request) { // @RequestBody: JSON body -> record
        try {
            // Unpack the DTO: the service gets plain values, never the request record
            return reservationService.create(
                    roomId, request.reservedBy(), request.startTime(), request.endTime())
                    // 201 Created + Location header telling the client where the new resource lives
                    .map(reservation -> ResponseEntity
                            .created(URI.create("/api/reservations/" + reservation.getId()))
                            .body(ReservationResponse.from(reservation)))
                    .orElseGet(() -> ResponseEntity.notFound().build()); // room missing -> 404
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build(); // bad name/times -> 400
        } catch (ConflictException e) {
            return ResponseEntity.status(409).build(); // overlaps an existing booking -> 409
        }
    }

    // GET /api/reservations?reservedBy=Mina -> 200 (an empty list is still 200)
    @Operation(summary = "List reservations by person")
    @ApiResponse(responseCode = "200", description = "Reservations ordered by start time")
    @GetMapping("/reservations")
    public List<ReservationResponse> search(@RequestParam String reservedBy) {
        // @RequestParam without required=false is required: missing ?reservedBy -> 400 automatically.
        // Returning a plain List (not ResponseEntity) means 200.
        return reservationService.findByReservedBy(reservedBy).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    // GET /api/reservations/{id} -> 200 or 404 (makes the Location from POST a real URL)
    @Operation(summary = "Find a reservation by id")
    @ApiResponse(responseCode = "200", description = "Found")
    @ApiResponse(responseCode = "404", description = "No such reservation")
    @GetMapping("/reservations/{id}")
    public ResponseEntity<ReservationResponse> findOne(@PathVariable Long id) {
        // Optional maps straight onto HTTP: present -> 200 with body, empty -> 404
        return reservationService.findById(id)
                .map(reservation -> ResponseEntity.ok(ReservationResponse.from(reservation)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // DELETE /api/reservations/{id} -> 204 or 404
    @Operation(summary = "Delete a reservation")
    @ApiResponse(responseCode = "204", description = "Deleted")
    @ApiResponse(responseCode = "404", description = "No such reservation")
    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { // Void = status only, no body
        return reservationService.delete(id)
                ? ResponseEntity.noContent().build()   // 204: done, nothing to send back
                : ResponseEntity.notFound().build();   // 404: nothing to delete
    }
}
