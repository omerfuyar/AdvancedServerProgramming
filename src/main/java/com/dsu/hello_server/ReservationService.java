package com.dsu.hello_server;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

// Business rules for reservations. Knows nothing about HTTP: it takes plain values and
// reports results that ApiExceptionHandler turns into status codes:
//   returns a value                 -> 200 / 201 / 204
//   throws IllegalArgumentException -> 400 (the request itself is wrong)
//   throws NotFoundException        -> 404 (room or reservation doesn't exist)
//   throws ConflictException        -> 409 (valid, but clashes with existing data)
@Service // Spring creates exactly one instance and injects it into ReservationController
public class ReservationService {

    private static final int MAX_RESERVED_BY_LENGTH = 50; // reservation.reserved_by is VARCHAR(50)

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository; // needed to answer 404 for a missing room

    // Constructor injection: Spring sees this (only) constructor and passes in both repository
    // beans. No @Autowired needed. final = set once, never swapped.
    public ReservationService(ReservationRepository reservationRepository, RoomRepository roomRepository) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
    }

    // Without the existsById check, room 99 would return [] with 200, which is wrong.
    // A room with no reservations is still 200 with [].
    public List<Reservation> findByRoom(Long roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new NotFoundException("Room " + roomId + " not found"); // 404
        }
        return reservationRepository.findByRoomIdOrderByStartTime(roomId);
    }

    // Uses the join fetch query, so one SQL statement no matter how many rooms are involved
    public List<Reservation> findByReservedBy(String reservedBy) {
        return reservationRepository.findByReservedByWithRoom(reservedBy);
    }

    public Reservation findById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reservation " + id + " not found")); // 404
    }

    // @Transactional: read (find room, count overlaps) and write (save) as one unit of work;
    // rolled back if a RuntimeException escapes. Spring applies it through a proxy around this bean.
    // (This is jakarta.transaction.Transactional; Spring's own
    // org.springframework.transaction.annotation.Transactional works the same here.)
    // Limit: it does NOT stop two simultaneous requests from both counting 0 and both inserting.
    // A real guarantee needs a row lock or a database constraint.
    @Transactional
    public Reservation create(
            Long roomId,
            String reservedBy,
            LocalDateTime startTime,
            LocalDateTime endTime) {
        // Fixed order: cheap checks first, then ask the database.
        checkRequest(reservedBy, startTime, endTime); // 400: throws before any SQL

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Room " + roomId + " not found")); // 404
        if (reservationRepository.countOverlapping(roomId, startTime, endTime) > 0) {
            throw new ConflictException("Room " + roomId + " is already reserved at that time"); // 409
        }
        // id is null -> save() does an INSERT, and the generated id is written back into the object
        return reservationRepository.save(new Reservation(room, reservedBy, startTime, endTime));
    }

    // deleteById does nothing for a missing id, so we check existence first to answer 404.
    public void delete(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new NotFoundException("Reservation " + id + " not found"); // 404
        }
        reservationRepository.deleteById(id); // returning normally = 204
    }

    // Validation of the request itself -> IllegalArgumentException -> 400.
    // One message per rule, so the ApiError says exactly what to fix.
    private void checkRequest(String reservedBy, LocalDateTime startTime, LocalDateTime endTime) {
        if (reservedBy == null || reservedBy.isBlank()) {
            throw new IllegalArgumentException("reservedBy is required");
        }
        if (reservedBy.length() > MAX_RESERVED_BY_LENGTH) {
            throw new IllegalArgumentException(
                    "reservedBy must be at most " + MAX_RESERVED_BY_LENGTH + " characters");
        }
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("startTime and endTime are required");
        }
        // isBefore (not !isAfter) also rejects start == end, a zero-length booking
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("startTime must be before endTime");
        }
    }
}
