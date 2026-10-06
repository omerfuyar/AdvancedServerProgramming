CREATE TABLE room (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    capacity INT NOT NULL,
    CONSTRAINT chk_room_capacity CHECK (
        capacity BETWEEN 1 AND 20
    )
);
CREATE TABLE reservation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL,
    reserved_by VARCHAR(50) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    CONSTRAINT fk_reservation_room FOREIGN KEY (room_id) REFERENCES room(id)
);