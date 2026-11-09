package com.dsu.hello_server;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

// Business rules for rooms: the filters, the name and capacity rules, what "update" and "delete" mean.
// Never returns ResponseEntity or status codes. It speaks in exceptions that ApiExceptionHandler maps:
//   IllegalArgumentException -> 400, NotFoundException -> 404, ConflictException -> 409.
@Service
public class RoomService {

    // The rules live in ONE place, so POST and PUT can never disagree
    private static final int MIN_CAPACITY = 1;
    private static final int MAX_CAPACITY = 20;
    private static final int MAX_NAME_LENGTH = 100; // room.name is VARCHAR(100)

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository; // added in Week 7 for the delete check

    // Constructor injection: Spring passes in the repository beans it generated
    public RoomService(RoomRepository roomRepository, ReservationRepository reservationRepository) {
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
    }

    // Picks the query method that matches the given filters, so filtering happens in SQL (WHERE),
    // not by loading every row into Java and streaming over it.
    public List<Room> search(Integer minCapacity, String keyword) {
        boolean hasKeyword = keyword != null && !keyword.isBlank(); // ?keyword= (empty) counts as absent
        if (minCapacity == null && !hasKeyword) {
            return roomRepository.findAll();                                  // no WHERE
        }
        if (minCapacity == null) {
            return roomRepository.findByNameContainingIgnoreCase(keyword);    // name LIKE
        }
        if (!hasKeyword) {
            return roomRepository.findByCapacityGreaterThanEqual(minCapacity); // capacity >=
        }
        return roomRepository.findByCapacityGreaterThanEqualAndNameContainingIgnoreCase(
                minCapacity, keyword);                                        // both
    }

    public long count() {
        return roomRepository.count(); // select count(*) from room
    }

    public Room findById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room " + id + " not found")); // 404
    }

    public Room create(String name, int capacity) {
        checkRequest(name, capacity); // 400 before touching the database
        // id null -> save() INSERTs; the returned Room carries the generated id
        return roomRepository.save(new Room(name, capacity));
    }

    // PUT. Only existing rooms can be updated: missing id -> 404 (not "create it").
    // Dirty checking, no save():
    //  1. findById inside the transaction returns a MANAGED Room; Hibernate keeps a snapshot of its fields
    //  2. room.update(...) only changes the Java object
    //  3. when the method returns, @Transactional commits; Hibernate flushes, compares the object with
    //     the snapshot, and sends "update room set capacity=?, name=? where id=?" only if a field changed.
    // Same values again (request 30) -> one select, no update.
    @Transactional
    public Room update(Long id, String name, int capacity) {
        checkRequest(name, capacity); // 400 first: cheap, no SQL
        Room room = findById(id);     // 404 if missing; otherwise managed
        room.update(name, capacity);
        return room;
    }

    public void delete(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new NotFoundException("Room " + id + " not found"); // 404
        }
        // Week 7, strategy B "ask permission": check first, then act.
        if (reservationRepository.existsByRoomId(id)) {
            throw new ConflictException("Room " + id + " has reservations"); // 409
        }
        // Strategy A still guards behind it: if a reservation slips in between the check and the delete,
        // the foreign key refuses, Spring raises DataIntegrityViolationException, and
        // ApiExceptionHandler answers 409 as well.
        roomRepository.deleteById(id); // returning normally = 204
    }

    private void checkRequest(String name, int capacity) {
        // Service speaks Java, not HTTP: ApiExceptionHandler maps these to 400.
        // Without the name check a missing name would reach the NOT NULL column and fail as a database error.
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("name must be at most " + MAX_NAME_LENGTH + " characters");
        }
        if (capacity < MIN_CAPACITY || capacity > MAX_CAPACITY) {
            throw new IllegalArgumentException(
                    "capacity must be between " + MIN_CAPACITY + " and " + MAX_CAPACITY);
        }
    }
}
