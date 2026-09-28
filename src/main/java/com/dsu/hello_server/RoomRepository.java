package com.dsu.hello_server;

import java.util.List;
import java.util.Optional;

public interface RoomRepository {

    List<Room> findAll();

    Optional<Room> findById(Long id);

    Room save(Room room);

    boolean deleteById(Long id);
}
