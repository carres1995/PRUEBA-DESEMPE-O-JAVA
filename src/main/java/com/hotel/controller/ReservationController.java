package com.hotel.controller;

import com.hotel.model.Guest;
import com.hotel.model.Reservation;
import com.hotel.model.Room;
import com.hotel.model.enums.RoomStatus;
import com.hotel.service.GuestService;
import com.hotel.service.ReservationService;
import com.hotel.service.RoomService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for Reservation management operations.
 */
public class ReservationController {

    private final ReservationService reservationService;
    private final RoomService roomService;
    private final GuestService guestService;

    public ReservationController(ReservationService reservationService, RoomService roomService, GuestService guestService) {
        this.reservationService = reservationService;
        this.roomService = roomService;
        this.guestService = guestService;
    }

    public void handleCheckIn(Long roomId, Long guestId, LocalDate checkIn, LocalDate checkOut) {
        Reservation reservation = new Reservation();
        reservation.setRoomId(roomId);
        reservation.setGuestId(guestId);
        reservation.setCheckIn(checkIn);
        reservation.setCheckOut(checkOut);

        reservationService.createReservation(reservation);
    }

    public void handleCheckOut(Long reservationId) {
        reservationService.executeCheckout(reservationId);
    }

    public List<Reservation> listActive() {
        return reservationService.listActive();
    }

    public List<Room> getAvailableRooms() {
        return roomService.listByTypeAndStatus(null, RoomStatus.AVAILABLE);
    }

    public List<Guest> getActiveGuests() {
        return guestService.listAll().stream()
                .filter(Guest::isActive)
                .collect(Collectors.toList());
    }
}
