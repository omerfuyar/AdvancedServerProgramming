INSERT INTO room (name, capacity) VALUES ('Seminar A', 8);
INSERT INTO room (name, capacity) VALUES ('Study Pod', 4);
INSERT INTO room (name, capacity) VALUES ('Rooftop Room', 12);

INSERT INTO reservation (room_id, reserved_by, start_time, end_time) VALUES (1, 'Mina',  '2026-10-06 10:00:00', '2026-10-06 11:00:00');
INSERT INTO reservation (room_id, reserved_by, start_time, end_time) VALUES (1, 'Omar',  '2026-10-06 13:00:00', '2026-10-06 15:00:00');
INSERT INTO reservation (room_id, reserved_by, start_time, end_time) VALUES (2, 'Mina',  '2026-10-06 09:00:00', '2026-10-06 10:00:00');
INSERT INTO reservation (room_id, reserved_by, start_time, end_time) VALUES (1, 'Lucas', '2026-10-07 10:00:00', '2026-10-07 12:00:00');
INSERT INTO reservation (room_id, reserved_by, start_time, end_time) VALUES (2, 'Aiko',  '2026-10-07 14:00:00', '2026-10-07 15:00:00');
