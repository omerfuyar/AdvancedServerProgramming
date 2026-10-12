package com.dsu.hello_server;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository
        extends JpaRepository<Room, Long> {
    List<Room> findByNameContainingIgnoreCase(String keyword);

    List<Room> findByCapacityGreaterThanEqual(int minCapacity);

    List<Room> findByCapacityGreaterThanEqualAndNameContainingIgnoreCase(
            int minCapacity, String keyword);
}