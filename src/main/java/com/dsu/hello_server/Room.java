package com.dsu.hello_server;

import jakarta.persistence.*;

@Entity
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int capacity;

    protected Room() {
    } // for Hibernate

    public Room(Long id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
    }

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