package com.dsu.hello_server;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

// Business rules for reservations. Knows nothing about HTTP: it takes plain values and
// reports results that ApiExceptionHandler turns into status codes:
//   returns a value                 -> 200 / 201 / 204
//   throws IllegalArgumentException -> 400 (the request itself is wrong)
//   throws ForbiddenException       -> 403 (not the caller's reservation, rule C)
//   throws NotFoundException        -> 404 (room or reservation doesn't exist)
//   throws ConflictException        -> 409 (valid, but clashes with existing data)
// It does not read Spring Security: the controller passes the caller's username and whether
// the caller is an admin.
@Service // Spring creates exactly one instance and injects it into ReservationController
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository; // needed to answer 404 for a missing room
    private final AppUserRepository userRepository; // the owner of a new reservation

    // Constructor injection: Spring sees this (only) constructor and passes in the repository
    // beans. No @Autowired needed. final = set once, never swapped.
    public ReservationService(ReservationRepository reservationRepository, RoomRepository roomRepository,
            AppUserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    // Without the existsById check, room 99 would return [] with 200, which is wrong.
    // A room with no reservations is still 200 with [].
    public List<Reservation> findByRoom(Long roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new NotFoundException("Room " + roomId + " not found"); // 404
        }
        return reservationRepository.findByRoomIdOrderByStartTime(roomId);
    }

    // The caller's own reservations. One join fetch query for rooms and owner.
    public List<Reservation> findMine(String username) {
        return reservationRepository.findMine(username);
    }

    // Read one: the owner or an admin. 404 before 403: we must load it to know whose it is.
    public Reservation get(Long id, String username, boolean admin) {
        Reservation reservation = find(id);
        requireOwnerOrAdmin(reservation, username, admin);
        return reservation;
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
            String username,
            LocalDateTime startTime,
            LocalDateTime endTime) {
        // Fixed order: cheap checks first, then ask the database.
        checkTimes(startTime, endTime); // 400: throws before any SQL

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Room " + roomId + " not found")); // 404
        if (reservationRepository.countOverlapping(roomId, startTime, endTime) > 0) {
            throw new ConflictException("Room " + roomId + " is already reserved at that time"); // 409
        }
        // The owner comes from the login, never from the request body
        AppUser owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User " + username + " not found"));
        // id is null -> save() does an INSERT, and the generated id is written back into the object
        return reservationRepository.save(new Reservation(room, owner, startTime, endTime));
    }

    // Lab G: move a reservation. Only its owner, not even an admin.
    // 400 -> 404 -> 403 -> 409. No save(): the reservation is managed, dirty checking writes it.
    @Transactional
    public Reservation reschedule(Long id, String username, LocalDateTime startTime, LocalDateTime endTime) {
        checkTimes(startTime, endTime);                          // 400
        Reservation reservation = find(id);                      // 404
        if (!reservation.isOwnedBy(username)) {                  // 403
            throw new ForbiddenException("reservation " + id + " belongs to another user");
        }
        Long roomId = reservation.getRoom().getId();
        if (reservationRepository.countOverlappingExcept(roomId, id, startTime, endTime) > 0) {
            throw new ConflictException("Room " + roomId + " is already reserved at that time"); // 409
        }
        reservation.reschedule(startTime, endTime);
        return reservation;
    }

    // Cancel: the owner or an admin.
    @Transactional
    public void delete(Long id, String username, boolean admin) {
        Reservation reservation = find(id);                      // 404 first
        requireOwnerOrAdmin(reservation, username, admin);       // then 403
        reservationRepository.delete(reservation);               // returning normally = 204
    }

    private Reservation find(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reservation " + id + " not found")); // 404
    }

    private void requireOwnerOrAdmin(Reservation reservation, String username, boolean admin) {
        if (!admin && !reservation.isOwnedBy(username)) {
            throw new ForbiddenException("reservation " + reservation.getId() + " belongs to another user");
        }
    }

    // Validation of the request itself -> IllegalArgumentException -> 400.
    // One message per rule, so the ApiError says exactly what to fix.
    private void checkTimes(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("startTime and endTime are required");
        }
        // isBefore (not !isAfter) also rejects start == end, a zero-length booking
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("startTime must be before endTime");
        }
    }
}
