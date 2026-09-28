# room-api

## Endpoints

| Method | Path | Status codes | Description |
| --- | --- | --- | --- |
| GET | `/api/rooms` | 200, 400 | List all rooms. Accepts `minCapacity` and `keyword`, alone or together; `keyword` matches the name and ignores case. 400 if `minCapacity` is not a number. |
| GET | `/api/rooms/{id}` | 200, 404 | Get one room by id. |
| POST | `/api/rooms` | 201, 400 | Create a room from `{"name": "...", "capacity": 10}`. Answers 201 with a `Location: /api/rooms/{id}` header, or 400 when capacity is outside 1–20. |
| PUT | `/api/rooms/{id}` | 200, 400, 404 | Replace an existing room. 400 when capacity is outside 1–20, 404 when there is no room with that id. |
| DELETE | `/api/rooms/{id}` | 204, 404 | Delete a room. |

## JDK

I used OpenJDK 26.0.2.

## Run

```
./gradlew bootRun
```

on Windows:

```
gradlew.bat bootRun
```

The server starts on http://localhost:8080. Open `api.http` and run end points

## AI use

I used AI to write the code and to format this file, then inspected and refactored it.
