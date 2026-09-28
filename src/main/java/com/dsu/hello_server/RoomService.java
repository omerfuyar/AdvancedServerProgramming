package com.dsu.hello_server;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public List<Room> search(Integer minCapacity, String keyword) {
        return roomRepository.findAll().stream()
                .filter(room -> minCapacity == null || room.capacity() >= minCapacity)
                .filter(room -> keyword == null || keyword.isBlank()
                        || room.name().toLowerCase().contains(keyword.toLowerCase()))
                .toList();
    }

    public Optional<Room> findById(Long id) {
        return roomRepository.findById(id);
    }

    public Room create(String name, int capacity) {
        return roomRepository.save(new Room(null, name, capacity));
    }

    public Optional<Room> replace(Long id, String name, int capacity) {
        return roomRepository.findById(id)
                .map(old -> roomRepository.save(new Room(id, name, capacity)));
    }

    public boolean delete(Long id) {
        return roomRepository.deleteById(id);
    }
}
