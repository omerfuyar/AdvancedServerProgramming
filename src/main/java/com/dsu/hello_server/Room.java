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

    // Our constructor. new Room(null, ...) = new room (INSERT); new Room(id, ...) = existing row (merge -> UPDATE)
    public Room(Long id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
    }

    // Jackson builds the JSON from these getters: {"id":..,"name":..,"capacity":..}
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
