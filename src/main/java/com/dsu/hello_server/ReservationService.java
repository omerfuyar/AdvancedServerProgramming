package com.dsu.hello_server;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;

    public ReservationService(ReservationRepository reservationRepository, RoomRepository roomRepository) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
    }

    public Optional<List<Reservation>> findByRoom(Long roomId) {
        if (!roomRepository.existsById(roomId)) {
            return Optional.empty();
        }
        return Optional.of(reservationRepository.findByRoomIdOrderByStartTime(roomId));
    }

    public List<Reservation> findByReservedBy(String reservedBy) {
        return reservationRepository.findByReservedByWithRoom(reservedBy);
    }

    public Optional<Reservation> findById(Long id) {
        return reservationRepository.findById(id);
    }

    @Transactional
    public Optional<Reservation> create(
            Long roomId,
            String reservedBy,
            LocalDateTime startTime,
            LocalDateTime endTime) {
        checkRequest(reservedBy, startTime, endTime);

        Optional<Room> room = roomRepository.findById(roomId);
        if (room.isEmpty()) {
            return Optional.empty();
        }
        if (reservationRepository.countOverlapping(roomId, startTime, endTime) > 0) {
            throw new ConflictException("Room " + roomId + " is already reserved at that time");
        }
        return Optional.of(reservationRepository.save(
                new Reservation(room.get(), reservedBy, startTime, endTime)));
    }

    public boolean delete(Long id) {
        if (!reservationRepository.existsById(id)) {
            return false;
        }
        reservationRepository.deleteById(id);
        return true;
    }

    private void checkRequest(String reservedBy, LocalDateTime startTime, LocalDateTime endTime) {
        if (reservedBy == null || reservedBy.isBlank()
                || startTime == null || endTime == null
                || !startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("reservedBy and a valid time range are required");
        }
    }
}
