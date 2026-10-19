package com.dsu.hello_server;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// Implemented by Spring Data at startup, like RoomRepository.
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // Reservation has no roomId field: Spring reads "RoomId" as the path room.id.
    // SQL: where room_id = ? order by start_time  (sorting in SQL, not in Java)
    List<Reservation> findByRoomIdOrderByStartTime(Long roomId);

    // First version of the "by person" search. It causes N+1 queries (1 for the list + 1 per room
    // when ReservationResponse.from() calls getName()). Not used any more: replaced by
    // findByReservedByWithRoom below.
    List<Reservation> findByReservedByOrderByStartTime(String reservedBy);

    // select count(...) > 0 ... where room_id = ?  -> used by RoomService.delete for the 409
    boolean existsByRoomId(Long roomId);

    // The overlap rule, written in JPQL (entity and field names, not table and column names).
    // Two bookings overlap when: new.start < old.end AND new.end > old.start.
    // Strict < and > mean touching bookings (11:00-12:00 right after 10:00-11:00) do NOT overlap.
    // :roomId etc. are named parameters; @Param ties each one to a method argument.
    // Hibernate sends values separately from the SQL (the ? marks), which also prevents SQL injection.
    @Query("""
            select count(r) from Reservation r
            where r.room.id = :roomId
              and r.startTime < :endTime
              and r.endTime > :startTime
            """)
    long countOverlapping(
            @Param("roomId") Long roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    // The fix for N+1: "join fetch" joins room in the same SQL and fills the room field
    // with real Room objects instead of proxies. One select instead of 1 + number of rooms.
    @Query("""
            select r from Reservation r
            join fetch r.room
            where r.reservedBy = :name
            order by r.startTime
            """)
    List<Reservation> findByReservedByWithRoom(@Param("name") String name);
}
