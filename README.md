# room-api

A small Spring Boot REST API for managing rooms (name and capacity), stored in memory.

## Endpoints

| Method | Path              | Status                              | What it does                                   |
|--------|-------------------|-------------------------------------|------------------------------------------------|
| GET    | /api/rooms        | 200                                 | List all rooms                                 |
| GET    | /api/rooms/{id}   | 200 / 404                           | Get one room                                   |
| POST   | /api/rooms        | 201 + `Location: /api/rooms/{id}`   | Create a room from `{"name": "...", "capacity": 10}` |
| PUT    | /api/rooms/{id}   | 200 / 404                           | Replace an existing room                       |
| DELETE | /api/rooms/{id}   | 204 / 404                           | Delete a room                                  |

## Run

Requires JDK 26 (the Gradle wrapper downloads everything else).

```
./gradlew bootRun
```

The server starts on http://localhost:8080. Try the requests in `api.http`.
