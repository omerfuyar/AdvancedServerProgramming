package com.dsu.hello_server;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

// HTTP layer only: maps URLs to service calls and service results to status codes.
// Errors are exceptions from ReservationService, answered by ApiExceptionHandler with an ApiError body.
// Every success returns ReservationResponse, never the Reservation entity.
// URL design: nested (/rooms/{roomId}/reservations) when the room matters (list, create),
// flat (/reservations/{id}) when the reservation's own id is enough (read, move, cancel).
// Authentication is the caller, filled in by Spring Security: getName() is the Basic username
// or the token's sub claim. The server decides who is calling, never the request body.
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
    public List<ReservationResponse> listForRoom(@PathVariable Long roomId) {
        // @PathVariable Long: Spring converts "1" -> 1L; "abc" -> 400 before this method runs
        return reservationService.findByRoom(roomId).stream() // missing room -> 404
                .map(ReservationResponse::from)
                .toList();
    }

    // POST /api/rooms/{roomId}/reservations -> 201 / 400 / 404 / 409
    @Operation(summary = "Create a reservation for a room")
    @ApiResponse(responseCode = "201", description = "Created, with a Location header; the caller is the owner")
    @ApiResponse(responseCode = "400", description = "Invalid reservation request")
    @ApiResponse(responseCode = "404", description = "No such room")
    @ApiResponse(responseCode = "409", description = "Reservation overlaps an existing reservation")
    @PostMapping("/rooms/{roomId}/reservations")
    public ResponseEntity<ReservationResponse> create(
            @PathVariable Long roomId,
            @RequestBody ReservationCreateRequest request, // @RequestBody: JSON body -> record
            Authentication authentication) {
        // Unpack the DTO: the service gets plain values, never the request record.
        // 400 (bad times), 404 (no room), 409 (overlap) are thrown by the service.
        Reservation reservation = reservationService.create(
                roomId, authentication.getName(), request.startTime(), request.endTime());
        // 201 Created + Location header telling the client where the new resource lives
        return ResponseEntity
                .created(URI.create("/api/reservations/" + reservation.getId()))
                .body(ReservationResponse.from(reservation));
    }

    // GET /api/reservations/mine -> 200 (an empty list is still 200). Replaces ?reservedBy=.
    // "mine" is never read as an {id}: a fixed path wins over a variable.
    @Operation(summary = "List the caller's reservations")
    @ApiResponse(responseCode = "200", description = "Reservations ordered by start time")
    @ApiResponse(responseCode = "401", description = "Login required")
    @GetMapping("/reservations/mine")
    public List<ReservationResponse> mine(Authentication authentication) {
        return reservationService.findMine(authentication.getName()).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    // GET /api/reservations/{id} -> 200 / 403 / 404 (owner or admin)
    @Operation(summary = "Find a reservation by id")
    @ApiResponse(responseCode = "200", description = "Found")
    @ApiResponse(responseCode = "403", description = "Not the owner and not an admin")
    @ApiResponse(responseCode = "404", description = "No such reservation")
    @GetMapping("/reservations/{id}")
    public ReservationResponse findOne(@PathVariable Long id, Authentication authentication) {
        return ReservationResponse.from(reservationService.get(
                id, authentication.getName(), isAdmin(authentication))); // missing -> 404
    }

    // PUT /api/reservations/{id} -> 200 / 400 / 403 / 404 / 409 (Lab G, owner only)
    @Operation(summary = "Move a reservation to a new time")
    @ApiResponse(responseCode = "200", description = "Moved")
    @ApiResponse(responseCode = "400", description = "Invalid times")
    @ApiResponse(responseCode = "403", description = "Not the owner")
    @ApiResponse(responseCode = "404", description = "No such reservation")
    @ApiResponse(responseCode = "409", description = "The new time overlaps another reservation")
    @PutMapping("/reservations/{id}")
    public ReservationResponse reschedule(@PathVariable Long id,
            @RequestBody ReservationCreateRequest request,
            Authentication authentication) {
        return ReservationResponse.from(reservationService.reschedule(
                id, authentication.getName(), request.startTime(), request.endTime()));
    }

    // DELETE /api/reservations/{id} -> 204 / 403 / 404 (owner or admin)
    @Operation(summary = "Delete a reservation")
    @ApiResponse(responseCode = "204", description = "Deleted")
    @ApiResponse(responseCode = "403", description = "Not the owner and not an admin")
    @ApiResponse(responseCode = "404", description = "No such reservation")
    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) { // Void = no body
        reservationService.delete(id, authentication.getName(), isAdmin(authentication)); // 404, 403
        return ResponseEntity.noContent().build();  // 204: done, nothing to send back
    }

    // Works for Basic and tokens: both give ROLE_ authorities
    private static boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
