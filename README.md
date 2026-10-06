# room-api

## Endpoints

| Method | Path              | Status codes  | Description                                                                                                                                                |
| ------ | ----------------- | ------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------- |
| GET    | `/api/rooms`      | 200, 400      | List all rooms. Accepts `minCapacity` and `keyword`, alone or together; `keyword` matches the name and ignores case. 400 if `minCapacity` is not a number. |
| GET    | `/api/rooms/{id}` | 200, 404      | Get one room by id.                                                                                                                                        |
| POST   | `/api/rooms`      | 201, 400      | Create a room from `{"name": "...", "capacity": 10}`. Answers 201 with a `Location: /api/rooms/{id}` header, or 400 when capacity is outside 1–20.         |
| PUT    | `/api/rooms/{id}` | 200, 400, 404 | Replace an existing room. 400 when capacity is outside 1–20, 404 when there is no room with that id.                                                       |
| DELETE | `/api/rooms/{id}` | 204, 404      | Delete a room.                                                                                                                                             |

## JDK

I used OpenJDK 26.0.2.

## Run

``` shell
./gradlew bootRun
```

on Windows:

``` shell
gradlew.bat bootRun
```

The server starts on <http://localhost:8080>. Open `api.http` and run end points

## Database

The application uses a MySQL database named `roomdb`. `schema.sql` describes
the tables, and `data.sql` contains the three rooms and five reservations from
class.

| Table         | Columns                                                                                           |
| ------------- | ------------------------------------------------------------------------------------------------- |
| `room`        | `id` (primary key), `name`, `capacity`                                                            |
| `reservation` | `id` (primary key), `room_id` (foreign key to `room.id`), `reserved_by`, `start_time`, `end_time` |

One room can have many reservations. Each reservation belongs to one room through
`reservation.room_id`.

Create the database and user using `src/main/resources/new.sql`, then start the
server with MySQL running locally. Use these connection settings:

| Setting   | Value                                |
| --------- | ------------------------------------ |
| JDBC URL  | `jdbc:mysql://localhost:3306/roomdb` |
| User Name | `roomapp`                            |
| Password  | `roomapp1234`                        |

'roomapp'@'localhost'
'roomapp1234'

## AI use

I used AI to write the code and to format this file, then inspected and refactored it.
