-- Q1. Rooms for 6 or more people, largest first.
SELECT *
FROM room
WHERE capacity >= 6
ORDER BY capacity DESC;

-- Q2. Every reservation made by Mina.
SELECT *
FROM reservation
WHERE reserved_by = 'Mina';

-- Q3. Every reservation with the name of its room.
SELECT r.*, rm.name AS room_name
FROM reservation AS r
JOIN room AS rm ON rm.id = r.room_id;

-- Q4. Reservations for Seminar A on 6 October 2026.
SELECT r.*, rm.name AS room_name
FROM reservation AS r
JOIN room AS rm ON rm.id = r.room_id
WHERE rm.name = 'Seminar A'
  AND r.start_time < '2026-10-07 00:00:00'
  AND r.end_time > '2026-10-06 00:00:00';

-- Q5. Number of reservations per room (JOIN).
SELECT rm.name, COUNT(r.id) AS reservation_count
FROM room AS rm
JOIN reservation AS r ON r.room_id = rm.id
GROUP BY rm.id, rm.name;

-- Q6. Number of reservations per room, including rooms with none.
SELECT rm.name, COUNT(r.id) AS reservation_count
FROM room AS rm
LEFT JOIN reservation AS r ON r.room_id = rm.id
GROUP BY rm.id, rm.name;

-- Q7. Rooms that have never been reserved.
SELECT rm.*
FROM room AS rm
WHERE NOT EXISTS (
    SELECT 1
    FROM reservation AS r
    WHERE r.room_id = rm.id
);

-- Q8. Rooms with more than two reservations.
SELECT rm.name, COUNT(r.id) AS reservation_count
FROM room AS rm
JOIN reservation AS r ON r.room_id = rm.id
GROUP BY rm.id, rm.name
HAVING COUNT(r.id) > 2;

-- Challenge. Reservations in room 1 overlapping 10:30–11:30 on 6 October 2026.
SELECT *
FROM reservation
WHERE room_id = 1
  AND start_time < '2026-10-06 11:30:00'
  AND end_time > '2026-10-06 10:30:00';
