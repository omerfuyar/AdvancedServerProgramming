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

@Tag(name = "Reservations", description = "Room reservations")
@RestController
@RequestMapping("/api")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Operation(summary = "List reservations for a room")
    @ApiResponse(responseCode = "200", description = "Reservations ordered by start time")
    @ApiResponse(responseCode = "404", description = "No such room")
    @GetMapping("/rooms/{roomId}/reservations")
    public ResponseEntity<List<ReservationResponse>> listForRoom(@PathVariable Long roomId) {
        return reservationService.findByRoom(roomId)
                .map(reservations -> ResponseEntity.ok(
                        reservations.stream().map(ReservationResponse::from).toList()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a reservation for a room")
    @ApiResponse(responseCode = "201", description = "Created, with a Location header")
    @ApiResponse(responseCode = "400", description = "Invalid reservation request")
    @ApiResponse(responseCode = "404", description = "No such room")
    @ApiResponse(responseCode = "409", description = "Reservation overlaps an existing reservation")
    @PostMapping("/rooms/{roomId}/reservations")
    public ResponseEntity<ReservationResponse> create(
            @PathVariable Long roomId,
            @RequestBody ReservationCreateRequest request) {
        try {
            return reservationService.create(
                    roomId, request.reservedBy(), request.startTime(), request.endTime())
                    .map(reservation -> ResponseEntity
                            .created(URI.create("/api/reservations/" + reservation.getId()))
                            .body(ReservationResponse.from(reservation)))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (ConflictException e) {
            return ResponseEntity.status(409).build();
        }
    }

    @Operation(summary = "List reservations by person")
    @ApiResponse(responseCode = "200", description = "Reservations ordered by start time")
    @GetMapping("/reservations")
    public List<ReservationResponse> search(@RequestParam String reservedBy) {
        return reservationService.findByReservedBy(reservedBy).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Operation(summary = "Find a reservation by id")
    @ApiResponse(responseCode = "200", description = "Found")
    @ApiResponse(responseCode = "404", description = "No such reservation")
    @GetMapping("/reservations/{id}")
    public ResponseEntity<ReservationResponse> findOne(@PathVariable Long id) {
        return reservationService.findById(id)
                .map(reservation -> ResponseEntity.ok(ReservationResponse.from(reservation)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete a reservation")
    @ApiResponse(responseCode = "204", description = "Deleted")
    @ApiResponse(responseCode = "404", description = "No such reservation")
    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return reservationService.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
