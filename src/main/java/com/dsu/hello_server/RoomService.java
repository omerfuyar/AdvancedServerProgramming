package com.dsu.hello_server;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

// Business rules for rooms: the filters, the capacity 1-20 rule, what "replace" and "delete" mean.
// Never returns ResponseEntity or status codes: Optional.empty()/false means "not found",
// IllegalArgumentException means "bad request", ConflictException means "conflict".
@Service
public class RoomService {

    // The rule lives in ONE place, so POST and PUT can never disagree
    private static final int MIN_CAPACITY = 1;
    private static final int MAX_CAPACITY = 20;

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

    public Optional<Room> findById(Long id) {
        return roomRepository.findById(id);
    }

    public Room create(String name, int capacity) {
        checkCapacity(capacity); // 400 before touching the database
        // id null -> save() INSERTs; the returned Room carries the generated id
        return roomRepository.save(new Room(null, name, capacity));
    }

    // Replace = PUT. Only existing rooms can be replaced: missing id -> empty -> 404 (not "create it").
    // save() with an id is a merge: Hibernate SELECTs the row, compares, then UPDATEs if changed.
    // That is the select + update shown in the README for request 9.
    public Optional<Room> replace(Long id, String name, int capacity) {
        checkCapacity(capacity);
        return roomRepository.findById(id)
                .map(old -> roomRepository.save(new Room(id, name, capacity)));
    }

    public boolean delete(Long id) {
        if (!roomRepository.existsById(id)) {
            return false; // 404
        }
        // Week 7, strategy B "ask permission": check first, then act.
        if (reservationRepository.existsByRoomId(id)) {
            throw new ConflictException("Room " + id + " has reservations"); // 409
        }
        // Week 6, strategy A "ask forgiveness": the foreign key in the database still guards
        // the delete. If the check above was ever skipped (e.g. a reservation inserted in between),
        // MySQL refuses and Spring raises DataIntegrityViolationException.
        // Note: RoomController only catches ConflictException, so this path would answer 500.
        // Throwing ConflictException here too would make it 409.
        try {
            roomRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new RoomHasReservationsException(id, e);
        }
        return true; // 204
    }

    private void checkCapacity(int capacity) {
        if (capacity < MIN_CAPACITY || capacity > MAX_CAPACITY) {
            // Service speaks Java, not HTTP: the controller maps this to 400
            throw new IllegalArgumentException(
                    "capacity must be between " + MIN_CAPACITY + " and " + MAX_CAPACITY);
        }
    }
}
