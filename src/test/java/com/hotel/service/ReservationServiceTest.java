package com.hotel.service;

import com.hotel.dao.interfaces.IGuestDao;
import com.hotel.dao.interfaces.IReservationDao;
import com.hotel.dao.interfaces.IRoomDao;
import com.hotel.exception.ReservationException;
import com.hotel.model.*;
import com.hotel.model.enums.ReservationStatus;
import com.hotel.model.enums.RoomStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationService — Business Rule Tests")
class ReservationServiceTest {

    @Mock
    private IReservationDao reservationDao;
    @Mock
    private IRoomDao roomDao;
    @Mock
    private IGuestDao guestDao;

    @InjectMocks
    private ReservationService service;

    // =====================================================================
    // SCENARIO 1: Happy Path
    // =====================================================================
    @Test
    @DisplayName("Scenario 1: Should successfully create a reservation (Check-in)")
    void shouldCreateReservationSuccessfully() throws Exception {
        Room room = new Room(); room.setStatus(RoomStatus.AVAILABLE);
        Guest guest = new Guest(); guest.setActive(true);
        Reservation res = new Reservation();
        res.setRoomId(10L); res.setGuestId(5L);
        res.setCheckIn(LocalDate.of(2026, 5, 1));
        res.setCheckOut(LocalDate.of(2026, 5, 5));

        doReturn(Optional.of(room)).when(roomDao).findById(any(), eq(10L));
        doReturn(Optional.of(guest)).when(guestDao).findById(any(), eq(5L));
        doReturn(false).when(reservationDao).hasOverlap(any(), eq(10L), any(), any());
        doNothing().when(reservationDao).insert(any(), any());
        doNothing().when(roomDao).updateStatus(any(), eq(10L), eq(RoomStatus.OCCUPIED));

        Reservation result = service.createReservation(res);

        assertNotNull(result);
        assertEquals(ReservationStatus.CHECKIN, result.getStatus());
        verify(roomDao).updateStatus(any(), eq(10L), eq(RoomStatus.OCCUPIED));
    }

    // =====================================================================
    // SCENARIO 2: Null room/guest
    // =====================================================================
    @Test
    @DisplayName("Scenario 2: Should throw exception when room or guest is null")
    void shouldThrowExceptionWhenIdsAreNull() {
        Reservation res = new Reservation();
        res.setRoomId(null);

        ReservationException ex = assertThrows(ReservationException.class, () -> service.createReservation(res));
        assertEquals("Room and guest are required", ex.getMessage());
    }

    // =====================================================================
    // SCENARIO 3: Invalid Dates
    // =====================================================================
    @Test
    @DisplayName("Scenario 3: Should throw exception when checkIn >= checkOut")
    void shouldThrowExceptionWhenDatesInvalid() {
        Reservation res = new Reservation();
        res.setRoomId(1L); res.setGuestId(1L);
        res.setCheckIn(LocalDate.of(2026, 5, 10));
        res.setCheckOut(LocalDate.of(2026, 5, 5));

        ReservationException ex = assertThrows(ReservationException.class, () -> service.createReservation(res));
        assertEquals("Check-in date must be before check-out", ex.getMessage());
    }

    // =====================================================================
    // SCENARIO 4: Room not available
    // =====================================================================
    @Test
    @DisplayName("Scenario 4: Should throw exception when room is OCCUPIED")
    void shouldThrowExceptionWhenRoomNotAvailable() throws Exception {
        Room room = new Room(); room.setStatus(RoomStatus.OCCUPIED);
        Reservation res = new Reservation();
        res.setRoomId(1L); res.setGuestId(1L);
        res.setCheckIn(LocalDate.of(2026, 5, 1));
        res.setCheckOut(LocalDate.of(2026, 5, 2));

        doReturn(Optional.of(room)).when(roomDao).findById(any(), eq(1L));

        ReservationException ex = assertThrows(ReservationException.class, () -> service.createReservation(res));
        assertEquals("Room is not available", ex.getMessage());
    }

    // =====================================================================
    // SCENARIO 5: Inactive Guest
    // =====================================================================
    @Test
    @DisplayName("Scenario 5: Should throw exception when guest is inactive")
    void shouldThrowExceptionWhenGuestInactive() throws Exception {
        Room room = new Room(); room.setStatus(RoomStatus.AVAILABLE);
        Guest guest = new Guest(); guest.setActive(false);
        Reservation res = new Reservation();
        res.setRoomId(1L); res.setGuestId(1L);
        res.setCheckIn(LocalDate.of(2026, 5, 1));
        res.setCheckOut(LocalDate.of(2026, 5, 2));

        doReturn(Optional.of(room)).when(roomDao).findById(any(), eq(1L));
        doReturn(Optional.of(guest)).when(guestDao).findById(any(), eq(1L));

        ReservationException ex = assertThrows(ReservationException.class, () -> service.createReservation(res));
        assertEquals("Guest is not active in the system", ex.getMessage());
    }

    // =====================================================================
    // SCENARIO 6: Overlapping
    // =====================================================================
    @Test
    @DisplayName("Scenario 6: Should throw exception on date overlap")
    void shouldThrowExceptionOnOverlap() throws Exception {
        Room room = new Room(); room.setStatus(RoomStatus.AVAILABLE);
        Guest guest = new Guest(); guest.setActive(true);
        Reservation res = new Reservation();
        res.setRoomId(10L); res.setGuestId(1L);
        res.setCheckIn(LocalDate.of(2026, 5, 5));
        res.setCheckOut(LocalDate.of(2026, 5, 10));

        doReturn(Optional.of(room)).when(roomDao).findById(any(), eq(10L));
        doReturn(Optional.of(guest)).when(guestDao).findById(any(), eq(1L));
        doReturn(true).when(reservationDao).hasOverlap(any(), eq(10L), any(), any());

        ReservationException ex = assertThrows(ReservationException.class, () -> service.createReservation(res));
        assertEquals("The room already has a reservation in that period", ex.getMessage());
    }

    // =====================================================================
    // SCENARIO 7: Checkout without check-in
    // =====================================================================
    @Test
    @DisplayName("Scenario 7: Should throw exception when checkout status is not CHECKIN")
    void shouldThrowExceptionWhenCheckoutStatusInvalid() throws Exception {
        Reservation res = new Reservation();
        res.setStatus(ReservationStatus.FINISHED);
        doReturn(Optional.of(res)).when(reservationDao).findById(any(), eq(1L));

        ReservationException ex = assertThrows(ReservationException.class, () -> service.executeCheckout(1L));
        assertEquals("There is no active check-in for this reservation", ex.getMessage());
    }

    // =====================================================================
    // SCENARIO 8: Cost calculation
    // =====================================================================
    @Test
    @DisplayName("Scenario 8: Should calculate total cost correctly with VAT")
    void shouldCalculateCostCorrectly() throws Exception {
        Room room = new Room(); room.setPricePerNight(200000);
        Reservation res = new Reservation();
        res.setId(1L); res.setRoomId(10L);
        res.setCheckIn(LocalDate.of(2026, 5, 1));
        res.setCheckOut(LocalDate.of(2026, 5, 4)); // 3 nights
        res.setStatus(ReservationStatus.CHECKIN);

        doReturn(Optional.of(res)).when(reservationDao).findById(any(), eq(1L));
        doReturn(Optional.of(room)).when(roomDao).findById(any(), eq(10L));

        // Note: ConfigUtil returns 0.19 by default
        service.executeCheckout(1L);

        // 3 * 200000 * 1.19 = 714000
        verify(reservationDao).finish(any(), eq(1L), eq(714000.0));
    }

    // =====================================================================
    // SCENARIO 9: 1 night stay
    // =====================================================================
    @Test
    @DisplayName("Scenario 9: Should calculate correctly for 1 night")
    void shouldCalculateForOneNight() throws Exception {
        Room room = new Room(); room.setPricePerNight(100);
        Reservation res = new Reservation();
        res.setId(1L); res.setRoomId(10L);
        res.setCheckIn(LocalDate.of(2026, 5, 1));
        res.setCheckOut(LocalDate.of(2026, 5, 2)); // 1 night
        res.setStatus(ReservationStatus.CHECKIN);

        doReturn(Optional.of(res)).when(reservationDao).findById(any(), eq(1L));
        doReturn(Optional.of(room)).when(roomDao).findById(any(), eq(10L));

        service.executeCheckout(1L);
        verify(reservationDao).finish(any(), eq(1L), eq(119.0));
    }

    // =====================================================================
    // SCENARIO 10: Rollback (Manual simulation)
    // =====================================================================
    @Test
    @DisplayName("Scenario 10: Should rollback if status update fails")
    void shouldRollbackOnFailure() throws Exception {
        Room room = new Room(); room.setStatus(RoomStatus.AVAILABLE);
        Guest guest = new Guest(); guest.setActive(true);
        Reservation res = new Reservation();
        res.setRoomId(10L); res.setGuestId(1L);
        res.setCheckIn(LocalDate.of(2026, 5, 1));
        res.setCheckOut(LocalDate.of(2026, 5, 2));

        doReturn(Optional.of(room)).when(roomDao).findById(any(), eq(10L));
        doReturn(Optional.of(guest)).when(guestDao).findById(any(), eq(1L));
        
        // Simulating DB error on second update
        doThrow(new SQLException("DB Failure")).when(roomDao).updateStatus(any(), eq(10L), any());

        assertThrows(ReservationException.class, () -> service.createReservation(res));
        // rollback is handled internally by try-with-resources + catch block
    }
}
