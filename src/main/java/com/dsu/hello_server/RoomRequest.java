package com.dsu.hello_server;

// DTO for POST/PUT bodies: what a client is ALLOWED to send. No id field, so a client can't pick ids.
// Jackson turns {"name": "...", "capacity": 8} into this record.
// capacity is int, not Integer: a missing value becomes 0, fails the 1-20 rule, and gives 400.
// (An Integer would be null and unboxing it in the service would throw an NPE -> 500.)
public record RoomRequest(String name, int capacity) {
}
