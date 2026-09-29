-- 1
SELECT * FROM room WHERE capacity > 6
-- 2
SELECT * FROM reservation WHERE reserved_by = 'Mina'
-- 3
SELECT * FROM reservation JOIN room ON room.id = reservation.room_id
-- 4
SELECT * FROM reservation JOIN room ON room.id = reservation.room_id WHERE ((start_time < '2026-10-07 00:00' AND start_time >= '2026-10-06 00:00') OR (end_time >= '2026-10-06 00:00' AND end_time < '2026-10-07 00:00')) AND room.name = 'Seminar A'
-- 5
SELECT room.name, count(*) FROM room JOIN reservation ON reservation.room_id = room.id GROUP BY room.id, room.name
-- 6
-- 7
-- 8
