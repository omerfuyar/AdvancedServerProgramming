# room-api

## Endpoints

| Method | Path                                | Status codes       | Description                                                               |
| ------ | ----------------------------------- | ------------------ | ------------------------------------------------------------------------- |
| GET    | `/api/rooms`                        | 200, 400           | List rooms with optional `minCapacity` and `keyword` filters.             |
| GET    | `/api/rooms/{id}`                   | 200, 404           | Get one room by id.                                                       |
| POST   | `/api/rooms`                        | 201, 400           | Create a room; name is required and capacity must be 1–20.                |
| PUT    | `/api/rooms/{id}`                   | 200, 400, 404      | Update a room; capacity must be 1–20.                                     |
| DELETE | `/api/rooms/{id}`                   | 204, 404, 409      | Delete a room, or report a conflict when reservations still reference it. |
| GET    | `/api/rooms/stats`                  | 200                | Return the room count as `{"rooms": 3}`.                                  |
| GET    | `/api/rooms/{roomId}/reservations`  | 200, 404           | List a room's reservations ordered by start time.                         |
| POST   | `/api/rooms/{roomId}/reservations`  | 201, 400, 404, 409 | Book a room unless the time overlaps an existing reservation.             |
| GET    | `/api/reservations?reservedBy=Mina` | 200, 400           | List one person's reservations.                                           |
| GET    | `/api/reservations/{id}`            | 200, 404           | Get one reservation.                                                      |
| DELETE | `/api/reservations/{id}`            | 204, 404           | Cancel one reservation.                                                   |

## JDK

I used OpenJDK 21.0.12.1.

## Run

``` shell
./gradlew bootRun
```

on Windows:

``` shell
gradlew.bat bootRun
```

The server starts on <http://localhost:8080>. Run `reset.sql`, then open
`api.http` and run requests 1 to 43 in order. Requests 42 and 43 are my own
failure requests.

## Database

The application uses a MySQL database named `roomdb` as the `roomapp` user.
Run these three statements in MySQL as an administrator:

```sql
CREATE DATABASE roomdb;
CREATE USER 'roomapp'@'localhost' IDENTIFIED BY 'roomapp1234';
GRANT ALL PRIVILEGES ON roomdb.* TO 'roomapp'@'localhost';
```

Then run `reset.sql` as `roomapp` while using the `roomdb` database. For example:

```sh
mysql -u roomapp -p roomdb < reset.sql
```

Enter `roomapp1234` when prompted. `reset.sql` drops and recreates both tables
and inserts the three rooms and five reservations from class. Do this before
running `api.http`, because the request results assume this starting state.

| Table         | Columns                                                                                           |
| ------------- | ------------------------------------------------------------------------------------------------- |
| `room`        | `id` (primary key), `name`, `capacity`                                                            |
| `reservation` | `id` (primary key), `room_id` (foreign key to `room.id`), `reserved_by`, `start_time`, `end_time` |

One room can have many reservations. Each reservation belongs to one room through
`reservation.room_id`.

Reservation responses expose `roomId` and `roomName` rather than serializing the
JPA entity and its lazy room relationship. A booking is rejected with 409 when
its interval overlaps an existing reservation for the same room; intervals that
only touch at an endpoint are allowed.

Create the database and user using `src/main/resources/new.sql`, then start the
server with MySQL running locally. Use these connection settings:

| Setting   | Value                                |
| --------- | ------------------------------------ |
| JDBC URL  | `jdbc:mysql://localhost:3306/roomdb` |
| User Name | `roomapp`                            |
| Password  | `roomapp1234`                        |

Every endpoint returns a response record (`RoomResponse` or
`ReservationResponse`), never a JPA entity.

Deleting a room with reservations is checked in `RoomService.delete` first
(409). The foreign key in the database still guards behind that check: if it
ever refuses, `ApiExceptionHandler` also answers 409, never 500.

Request 19 succeeds because it starts at 11:00, exactly when Mina's booking
ends. The overlap check uses strict `<` and `>`, so touching bookings are allowed.

## PUT and dirty checking

`RoomService.update` is `@Transactional`. It loads the room with `findById`,
which returns a managed entity, changes it with `room.update(name, capacity)`,
and returns. There is no `save()` call. When the transaction commits, Hibernate
compares the room with the snapshot it took when loading it and sends an UPDATE
only if a field changed.

For request 9 (and 29), Hibernate prints one select followed by this update:

```sql
select r1_0.id, r1_0.capacity, r1_0.name
from room r1_0
where r1_0.id = ?;

update room
set capacity = ?, name = ?
where id = ?;
```

The select loads the room (and gives 404 if it is missing). Request 30 sends
the same body again: Hibernate prints only the select, because nothing changed.
Request 31 has a bad capacity and prints no SQL at all, because the body is
checked before the room is loaded.

## Errors

Every error response (4xx and 5xx) has the same JSON body, `ApiError`. It is
built in one place, `ApiExceptionHandler` (`@RestControllerAdvice`). Controllers
have no try/catch. Services throw exceptions, and the handler turns each one
into a status and an `ApiError`.

```java
public record ApiError(
        Instant timestamp, // when the error happened (UTC)
        int status,        // the HTTP status code, repeated in the body
        String error,      // the status reason phrase, e.g. "Not Found"
        String message,    // what was wrong with this request
        String path) {     // the URL that was requested
}
```

Example: request 18 books room 1 while Mina has it.

```json
{
  "timestamp": "2026-11-09T15:35:51.180022544Z",
  "status": 409,
  "error": "Conflict",
  "message": "Room 1 is already reserved at that time",
  "path": "/api/rooms/1/reservations"
}
```

| Status | Meaning in this API | Thrown by / caused by | Examples in `api.http` |
| ------ | ------------------- | --------------------- | ---------------------- |
| 400 Bad Request | The request itself is wrong. Fix the request and send it again. | `IllegalArgumentException` from a service (missing name, capacity not 1–20, missing `reservedBy`, start not before end), or Spring cannot read the request (`abc` for a number, invalid JSON, a missing required parameter). | 6, 13, 20, 31–37, 42, 43 |
| 404 Not Found | The room or reservation in the URL does not exist, or no endpoint has that URL. | `NotFoundException` from a service, or an unknown path. | 10, 12, 16, 21, 25, 27, 28, 40 |
| 409 Conflict | The request is valid, but it clashes with data that already exists. | `ConflictException` from a service (a booking overlaps, a room still has reservations), or the database refuses a write (`DataIntegrityViolationException`). | 18, 23, 38, 39 |
| 500 Internal Server Error | A bug on our side, not the client's fault. The message is always `"Unexpected error"`. The details go to the server console, never to the client. | Any other exception. | none: no request should cause it |

The order of checks in a service is always 400, then 404, then 409: cheap checks
on the request come first, before any SQL. That is why request 43 answers 400
for a room that does not exist.

## AI use

I used AI to write the code and to format this file, then inspected and refactored it.
