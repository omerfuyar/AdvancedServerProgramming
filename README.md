# room-api

## Endpoints

| Method | Path               | Status codes  | Description                                                               |
| ------ | ------------------ | ------------- | ------------------------------------------------------------------------- |
| GET    | `/api/rooms`       | 200, 400      | List rooms with optional `minCapacity` and `keyword` filters.             |
| GET    | `/api/rooms/{id}`  | 200, 404      | Get one room by id.                                                       |
| POST   | `/api/rooms`       | 201, 400      | Create a room; capacity must be 1–20.                                     |
| PUT    | `/api/rooms/{id}`  | 200, 404, 400 | Replace a room; capacity must be 1–20.                                    |
| DELETE | `/api/rooms/{id}`  | 204, 404, 409 | Delete a room, or report a conflict when reservations still reference it. |
| GET    | `/api/rooms/stats` | 200           | Return the room count as `{"rooms": 3}`.                                  |

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

Create the database and user using `src/main/resources/new.sql`, then start the
server with MySQL running locally. Use these connection settings:

| Setting   | Value                                |
| --------- | ------------------------------------ |
| JDBC URL  | `jdbc:mysql://localhost:3306/roomdb` |
| User Name | `roomapp`                            |
| Password  | `roomapp1234`                        |

The database rejects deleting a room with reservations, so the service layer
translates that integrity failure into a domain conflict and the controller
returns HTTP 409 instead of exposing a database error as HTTP 500.

For request 9, Hibernate prints one select followed by this update:

```sql
select r1_0.id, r1_0.capacity, r1_0.name
from room r1_0
where r1_0.id = ?;

update room
set capacity = ?, name = ?
where id = ?;
```

The select verifies that the room exists; JPA then updates the managed room
instead of inserting a new row.

## AI use

I used AI to write the code and to format this file, then inspected and refactored it.
