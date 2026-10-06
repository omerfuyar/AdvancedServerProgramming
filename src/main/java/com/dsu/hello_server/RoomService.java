package com.dsu.hello_server;

import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

@Service
public class RoomService {

    private static final int MIN_CAPACITY = 1;
    private static final int MAX_CAPACITY = 20;

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public Stream<Room> search(Integer minCapacity, String keyword) {
        return roomRepository.findAll().stream()
                .filter(room -> minCapacity == null || room.getCapacity() >= minCapacity)
                .filter(room -> keyword == null || keyword.isBlank()
                        || room.getName().toLowerCase().contains(keyword.toLowerCase()));
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
        roomRepository.deleteById(id);
        return true;
    }

    private void checkCapacity(int capacity) {
        if (capacity < MIN_CAPACITY || capacity > MAX_CAPACITY) {
            throw new IllegalArgumentException(
                    "capacity must be between " + MIN_CAPACITY + " and " + MAX_CAPACITY);
        }
    }
}
