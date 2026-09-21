# room-api

A small Spring Boot REST API for room management.

## Endpoints

GET /api/rooms           => 200                                 => List all rooms
GET /api/rooms/{id}      => 200 / 404                           => Get one room
POST /api/rooms          => 201 + `Location: /api/rooms/{id}`   => Create a room from `{"name": "...", "capacity": 10}`
PUT /api/rooms/{id}      => 200 / 404                           => Replace an existing room
DELETE /api/rooms/{id}   => 204 / 404                           => Delete a room

## Run

I used JDK 26, 

```
./gradlew bootRun
```

The server starts on http://localhost:8080. Try the requests in `api.http`.
