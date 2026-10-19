package com.dsu.hello_server;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

// No class implements this interface: Spring Data generates the implementation at startup.
// JpaRepository<Room, Long> = entity type + id type, and gives findAll, findById, save,
// deleteById, existsById, count for free.
public interface RoomRepository
        extends JpaRepository<Room, Long> {

    // Derived query methods: Spring parses the NAME at startup and writes the SQL.
    // Names use Java field names (name, capacity), not column names. A typo stops startup.

    // where upper(name) like upper('%keyword%')
    List<Room> findByNameContainingIgnoreCase(String keyword);

    // where capacity >= ?
    List<Room> findByCapacityGreaterThanEqual(int minCapacity);

    // where capacity >= ? and upper(name) like upper('%keyword%')
    List<Room> findByCapacityGreaterThanEqualAndNameContainingIgnoreCase(
            int minCapacity, String keyword);
}
