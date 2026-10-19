package com.dsu.hello_server;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// Maps the existing reservation table (we map it, we don't change it; ddl-auto=validate checks it).
@Entity
@Table(name = "reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
    private Long id;

    // The foreign key room_id becomes a reference to a Room object instead of a Long.
    // @ManyToOne: many reservations -> one room (read it from this class: "many of me, one of that").
    // fetch = LAZY: load the reservation now, the room only when someone calls a getter on it.
    //   Until then, `room` holds a Hibernate proxy that only knows the id.
    //   (The default for @ManyToOne is EAGER = an extra query every time, even if unused.)
    // optional = false: every reservation has a room (matches NOT NULL).
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id") // the column that holds the foreign key
    private Room room;

    // No @Column needed: Spring Boot's naming rule maps camelCase -> snake_case
    private String reservedBy;          // reserved_by  VARCHAR(50)
    private LocalDateTime startTime;    // start_time   DATETIME (date + time, no time zone)
    private LocalDateTime endTime;      // end_time     DATETIME

    // Deliberately one direction only: Room has no List<Reservation>.
    // That avoids endless JSON (room -> reservations -> room -> ...) and two sides to keep in sync.
    // "Reservations of room 1" is just a repository query (findByRoomId...).

    protected Reservation() {
    } // for Hibernate

    // No id parameter: a new reservation gets its id from the database on save().
    public Reservation(Room room, String reservedBy, LocalDateTime startTime, LocalDateTime endTime) {
        this.room = room;
        this.reservedBy = reservedBy;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getId() {
        return id;
    }

    public Room getRoom() {
        return room;
    }

    public String getReservedBy() {
        return reservedBy;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }
}
