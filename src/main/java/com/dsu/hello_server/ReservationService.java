package com.dsu.hello_server;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

// Business rules for reservations. Knows nothing about HTTP: it takes plain values and
// reports results the controller turns into status codes:
//   returns a value          -> 200 / 201
//   returns Optional.empty() -> 404 (room or reservation doesn't exist)
//   throws IllegalArgumentException -> 400 (the request itself is wrong)
//   throws ConflictException        -> 409 (valid, but clashes with existing data)
@Service // Spring creates exactly one instance and injects it into ReservationController
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository; // needed to answer 404 for a missing room

    // Constructor injection: Spring sees this (only) constructor and passes in both repository
    // beans. No @Autowired needed. final = set once, never swapped.
    public ReservationService(ReservationRepository reservationRepository, RoomRepository roomRepository) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
    }

    // Optional<List>: empty = no such room (404); present (maybe an empty list) = 200.
    // Without the existsById check, room 99 would return [] with 200, which is wrong.
    public Optional<List<Reservation>> findByRoom(Long roomId) {
        if (!roomRepository.existsById(roomId)) {
            return Optional.empty();
        }
        return Optional.of(reservationRepository.findByRoomIdOrderByStartTime(roomId));
    }

    // Uses the join fetch query, so one SQL statement no matter how many rooms are involved
    public List<Reservation> findByReservedBy(String reservedBy) {
        return reservationRepository.findByReservedByWithRoom(reservedBy);
    }

    public Optional<Reservation> findById(Long id) {
        return reservationRepository.findById(id);
    }

    // @Transactional: read (find room, count overlaps) and write (save) as one unit of work;
    // rolled back if a RuntimeException escapes. Spring applies it through a proxy around this bean.
    // (This is jakarta.transaction.Transactional; Spring's own
    // org.springframework.transaction.annotation.Transactional works the same here.)
    // Limit: it does NOT stop two simultaneous requests from both counting 0 and both inserting.
    // A real guarantee needs a row lock or a database constraint.
    @Transactional
    public Optional<Reservation> create(
            Long roomId,
            String reservedBy,
            LocalDateTime startTime,
            LocalDateTime endTime) {
        // Fixed order: cheap checks first, then ask the database.
        checkRequest(reservedBy, startTime, endTime); // 400: throws before any SQL

        Optional<Room> room = roomRepository.findById(roomId);
        if (room.isEmpty()) {
            return Optional.empty(); // 404
        }
        if (reservationRepository.countOverlapping(roomId, startTime, endTime) > 0) {
            throw new ConflictException("Room " + roomId + " is already reserved at that time"); // 409
        }
        // id is null -> save() does an INSERT, and the generated id is written back into the object
        return Optional.of(reservationRepository.save(
                new Reservation(room.get(), reservedBy, startTime, endTime)));
    }

    // false = nothing to delete (404); true = deleted (204).
    // deleteById returns void, so we check existence first to tell the two apart.
    public boolean delete(Long id) {
        if (!reservationRepository.existsById(id)) {
            return false;
        }
        reservationRepository.deleteById(id);
        return true;
    }

    // Validation of the request itself -> IllegalArgumentException -> 400.
    // isBefore (not !isAfter) also rejects start == end, a zero-length booking.
    private void checkRequest(String reservedBy, LocalDateTime startTime, LocalDateTime endTime) {
        if (reservedBy == null || reservedBy.isBlank()
                || startTime == null || endTime == null
                || !startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("reservedBy and a valid time range are required");
        }
    }
}
