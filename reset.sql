DROP TABLE IF EXISTS refresh_token;
DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS room;
DROP TABLE IF EXISTS app_user;

CREATE TABLE room (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    capacity INT NOT NULL,
    CONSTRAINT chk_room_capacity CHECK (capacity BETWEEN 1 AND 20)
);

CREATE TABLE reservation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL,
    reserved_by VARCHAR(50) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    CONSTRAINT fk_reservation_room FOREIGN KEY (room_id) REFERENCES room (id)
);

CREATE TABLE app_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(20) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    CONSTRAINT uq_app_user_username UNIQUE (username)
);

CREATE TABLE refresh_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token_hash VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    expires_at DATETIME NOT NULL,
    CONSTRAINT uq_refresh_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES app_user (id)
);

INSERT INTO room (name, capacity) VALUES ('Seminar A', 8);
INSERT INTO room (name, capacity) VALUES ('Study Pod', 4);
INSERT INTO room (name, capacity) VALUES ('Rooftop Room', 12);

INSERT INTO reservation (room_id, reserved_by, start_time, end_time)
VALUES (1, 'Mina', '2026-10-06 10:00:00', '2026-10-06 11:00:00');
INSERT INTO reservation (room_id, reserved_by, start_time, end_time)
VALUES (1, 'Omar', '2026-10-06 13:00:00', '2026-10-06 15:00:00');
INSERT INTO reservation (room_id, reserved_by, start_time, end_time)
VALUES (2, 'Mina', '2026-10-06 09:00:00', '2026-10-06 10:00:00');
INSERT INTO reservation (room_id, reserved_by, start_time, end_time)
VALUES (1, 'Lucas', '2026-10-07 10:00:00', '2026-10-07 12:00:00');
INSERT INTO reservation (room_id, reserved_by, start_time, end_time)
VALUES (2, 'Aiko', '2026-10-07 14:00:00', '2026-10-07 15:00:00');

-- BCrypt hashes only. Passwords: mina1234, omar1234, admin1234
INSERT INTO app_user (username, password_hash, role)
VALUES ('mina', '$2a$10$ix/.xals1cT5fBy84xYGd./rBgU.YjsOFZ0hS649iIxeUqTiiOLzK', 'USER');
INSERT INTO app_user (username, password_hash, role)
VALUES ('omar', '$2a$10$onWQt0PZDzEQM7QXfCOwIe8jD5ixW1ZH2pMmXqN9TOjB0qki0yNd2', 'USER');
INSERT INTO app_user (username, password_hash, role)
VALUES ('admin', '$2a$10$UQJ.C/3fccTCO0E0ztgfxeYEzsY2zwJ74tDbeLo6mzAfmARnmbYGC', 'ADMIN');
