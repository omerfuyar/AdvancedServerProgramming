# room-api

A small Spring Boot REST API for managing study rooms, built in three layers:

```
RoomController  ->  RoomService  ->  RoomRepository (interface)
   HTTP only         the rules         InMemoryRoomRepository
```

The controller knows only HTTP: paths, status codes, the `Location` header and the
request DTO. The service knows the rules: the filters and the capacity limit. The
repository knows where the data lives: the list and the id counter. Each class is
handed what it needs through its constructor, so no layer creates the one below it.

## Endpoints

| Method | Path | Status codes | Description |
| --- | --- | --- | --- |
| GET | `/api/rooms` | 200, 400 | List all rooms. Accepts `minCapacity` and `keyword`, alone or together; `keyword` matches the name and ignores case. 400 if `minCapacity` is not a number. |
| GET | `/api/rooms/{id}` | 200, 404 | Get one room by id. |
| POST | `/api/rooms` | 201, 400 | Create a room from `{"name": "...", "capacity": 10}`. Answers 201 with a `Location: /api/rooms/{id}` header, or 400 when capacity is outside 1–20. |
| PUT | `/api/rooms/{id}` | 200, 400, 404 | Replace an existing room. 400 when capacity is outside 1–20, 404 when there is no room with that id. |
| DELETE | `/api/rooms/{id}` | 204, 404 | Delete a room. |

**The capacity rule:** capacity must be between 1 and 20. It is checked once, in
`RoomService.checkCapacity`, and both POST and PUT answer 400 when it is broken.

The server starts with two rooms: `Room A` (capacity 10) and `Room B` (capacity 20).
They live in memory, so every restart brings them back and loses anything you created.

## API documentation

With the server running:

- Swagger UI — http://localhost:8080/swagger-ui.html
- OpenAPI specification — http://localhost:8080/v3/api-docs

## JDK

I used **JDK 26** (OpenJDK 26.0.2). The Gradle toolchain in `build.gradle` is set to 26.
Java 21 or newer is enough to build the project.

## Run

```
./gradlew bootRun
```

on Windows:

```
gradlew.bat bootRun
```

The server starts on http://localhost:8080. Open `api.http` and run the sixteen
requests from top to bottom against a freshly started server — each one says the
status code it should give.

## AI use

The three-layer refactor was written with help from Claude, then read, run and
committed by me.
