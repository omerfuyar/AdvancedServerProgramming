package com.dsu.hello_server;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class RoomService {

    private static final int MIN_CAPACITY = 1;
    private static final int MAX_CAPACITY = 20;

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public List<Room> search(Integer minCapacity, String keyword) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        if (minCapacity == null && !hasKeyword) {
            return roomRepository.findAll();
        }
        if (minCapacity == null) {
            return roomRepository.findByNameContainingIgnoreCase(keyword);
        }
        if (!hasKeyword) {
            return roomRepository.findByCapacityGreaterThanEqual(minCapacity);
        }
        return roomRepository.findByCapacityGreaterThanEqualAndNameContainingIgnoreCase(
                minCapacity, keyword);
    }

    public long count() {
        return roomRepository.count();
    }

    public Optional<Room> findById(Long id) {
        return roomRepository.findById(id);
    }

    public Room create(String name, int capacity) {
        checkCapacity(capacity);
        return roomRepository.save(new Room(null, name, capacity));
    }

    public Optional<Room> replace(Long id, String name, int capacity) {
        checkCapacity(capacity);
        return roomRepository.findById(id)
                .map(old -> roomRepository.save(new Room(id, name, capacity)));
    }

    public boolean delete(Long id) {
        if (!roomRepository.existsById(id)) {
            return false;
        }
        try {
            roomRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new RoomHasReservationsException(id, e);
        }
        return true;
    }

    private void checkCapacity(int capacity) {
        if (capacity < MIN_CAPACITY || capacity > MAX_CAPACITY) {
            throw new IllegalArgumentException(
                    "capacity must be between " + MIN_CAPACITY + " and " + MAX_CAPACITY);
        }
    }
}
