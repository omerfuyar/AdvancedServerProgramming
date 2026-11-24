package com.dsu.hello_server;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// ReservationService alone: the repositories are Mockito fakes, no Spring and no database.
// A service throws; ApiExceptionHandler turns it into a status, so these tests check exceptions.
class ReservationServiceTest {

    private RoomRepository roomRepository;
    private ReservationRepository reservationRepository;
    private AppUserRepository userRepository;
    private ReservationService service;

    private final Room room = new Room("Seminar A", 8);
    private final AppUser mina = new AppUser("mina", "hash", "USER");
    private final LocalDateTime ten = LocalDateTime.of(2026, 12, 1, 10, 0);
    private final LocalDateTime eleven = LocalDateTime.of(2026, 12, 1, 11, 0);

    @BeforeEach // runs before every test: fresh fakes each time
    void setUp() {
        roomRepository = mock(RoomRepository.class);
        reservationRepository = mock(ReservationRepository.class);
        userRepository = mock(AppUserRepository.class);
        service = new ReservationService(reservationRepository, roomRepository, userRepository);
    }

    @Test
    void createRefusesEndBeforeStart() { // 400
        assertThrows(IllegalArgumentException.class,
                () -> service.create(1L, "mina", eleven, ten));
        verify(reservationRepository, never()).save(any()); // and nothing was saved
    }

    @Test
    void createRefusesMissingRoom() { // 404
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.create(99L, "mina", ten, eleven));
    }

    @Test
    void createRefusesOverlap() { // 409
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(reservationRepository.countOverlapping(eq(1L), any(), any())).thenReturn(1L);

        assertThrows(ConflictException.class, () -> service.create(1L, "mina", ten, eleven));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void deleteRefusesAnotherUser() { // 403, rule C
        Reservation minas = new Reservation(room, mina, ten, eleven);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(minas));

        assertThrows(ForbiddenException.class, () -> service.delete(1L, "omar", false));
        verify(reservationRepository, never()).delete(any());
    }

    // ---------- Lab H1 ----------

    @Test
    void createSavesWithTheLoggedInOwner() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(reservationRepository.countOverlapping(eq(1L), any(), any())).thenReturn(0L);
        when(userRepository.findByUsername("mina")).thenReturn(Optional.of(mina));
        when(reservationRepository.save(any())).thenAnswer(call -> call.getArgument(0));

        Reservation saved = service.create(1L, "mina", ten, eleven);

        assertSame(mina, saved.getOwner());
        assertSame(room, saved.getRoom());
        assertEquals(ten, saved.getStartTime());
        assertEquals(eleven, saved.getEndTime());
    }

    @Test
    void ownerMayDelete() {
        Reservation minas = new Reservation(room, mina, ten, eleven);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(minas));

        assertDoesNotThrow(() -> service.delete(1L, "mina", false));
        verify(reservationRepository).delete(minas);
    }

    @Test
    void adminMayDeleteAnotherUsersReservation() {
        Reservation minas = new Reservation(room, mina, ten, eleven);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(minas));

        assertDoesNotThrow(() -> service.delete(1L, "admin", true));
        verify(reservationRepository).delete(minas);
    }

    @Test
    void adminMayNotReschedule() { // Lab G: only the owner moves it
        Reservation minas = new Reservation(room, mina, ten, eleven);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(minas));

        assertThrows(ForbiddenException.class,
                () -> service.reschedule(1L, "admin", ten.plusHours(2), eleven.plusHours(2)));
        assertEquals(ten, minas.getStartTime()); // not moved
    }
}
