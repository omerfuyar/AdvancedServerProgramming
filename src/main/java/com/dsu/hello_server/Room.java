package com.dsu.hello_server;

import jakarta.persistence.*;

// @Entity: Hibernate maps this class to a table; one Room object = one row.
// It cannot be a record: Hibernate needs a no-arg constructor and non-final fields
// (it creates an empty object, fills it from the row, and writes the generated id back after INSERT).
@Entity
@Table(name = "room") // table name; would default to "room" anyway, written for clarity
public class Room {

    @Id // primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL AUTO_INCREMENT hands out the id, not Java
    private Long id; // Long (not long) so null can mean "not saved yet" -> save() does INSERT

    // @Column only documents/validates the column; ddl-auto=validate checks it exists at startup
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    protected Room() {
    } // for Hibernate: it calls this via reflection, then sets the fields from the row

    // No id parameter: a new room gets its id from the database on save() (INSERT)
    public Room(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
    }

    // Used by RoomService.update (PUT). Called on a MANAGED room (loaded inside a @Transactional
    // method), so no save() is needed: at commit Hibernate compares the fields with the snapshot it
    // took when loading the row (dirty checking) and sends an UPDATE only if something changed.
    public void update(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
    }

    // Read by RoomResponse.from; the entity itself is never written as JSON
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }
}
