package com.dsu.hello_server;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository
        extends JpaRepository<Room, Long> {
}