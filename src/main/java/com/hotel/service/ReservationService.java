package com.hotel.service;

import com.hotel.config.ConnectionFactory;
import com.hotel.dao.interfaces.IGuestDao;
import com.hotel.dao.interfaces.IReservationDao;
import com.hotel.dao.interfaces.IRoomDao;
import com.hotel.exception.ReservationException;
import com.hotel.model.*;
import com.hotel.model.enums.HttpMethod;
import com.hotel.model.enums.ReservationStatus;
import com.hotel.model.enums.RoomStatus;
import com.hotel.util.AppLogger;
import com.hotel.util.ConfigUtil;
import com.hotel.util.UserSession;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service layer for Reservation management.
 */
public class ReservationService {

    private final IReservationDao reservationDao;
    private final IRoomDao roomDao;
    private final IGuestDao guestDao;

    public ReservationService(IReservationDao reservationDao, IRoomDao roomDao, IGuestDao guestDao) {
        this.reservationDao = reservationDao;
        this.roomDao = roomDao;
        this.guestDao = guestDao;
    }

    /**
     * Creates a new reservation (Check-in).
     */
    public Reservation createReservation(Reservation reservation) {
        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // BR-006: Room and guest required
                if (reservation.getRoomId() == null || reservation.getGuestId() == null) {
                    throw new ReservationException("Room and guest are required");
                }

                // BR-001: checkIn before checkOut
                if (reservation.getCheckIn() == null || reservation.getCheckOut() == null ||
                        !reservation.getCheckIn().isBefore(reservation.getCheckOut())) {
                    throw new ReservationException("Check-in date must be before check-out");
                }

                // BR-002: Room AVAILABLE
                Room room = roomDao.findById(conn, reservation.getRoomId())
                        .orElseThrow(() -> new ReservationException("Room not found"));
                if (room.getStatus() != RoomStatus.AVAILABLE) {
                    throw new ReservationException("Room is not available");
                }

                // BR-003: Guest ACTIVE
                Guest guest = guestDao.findById(conn, reservation.getGuestId())
                        .orElseThrow(() -> new ReservationException("Guest not found"));
                if (!guest.isActive()) {
                    throw new ReservationException("Guest is not active in the system");
                }

                // BR-004: No Overlap
                if (reservationDao.hasOverlap(conn, reservation.getRoomId(), reservation.getCheckIn(), reservation.getCheckOut())) {
                    throw new ReservationException("The room already has a reservation in that period");
                }

                // Create reservation
                reservation.setUserId(UserSession.getCurrentUser().getId());
                reservation.setStatus(ReservationStatus.CHECKIN);
                reservation.setCreatedAt(LocalDateTime.now());
                reservationDao.insert(conn, reservation);

                // Update room to OCCUPIED
                roomDao.updateStatus(conn, reservation.getRoomId(), RoomStatus.OCCUPIED);

                conn.commit();
                Long uid = UserSession.getCurrentUser() != null ? UserSession.getCurrentUser().getId() : null;
                AppLogger.log(HttpMethod.POST, "/reservations",
                    "Check-in: room " + reservation.getRoomId() + ", guest " + reservation.getGuestId(), 201, uid);
                return reservation;
            } catch (Exception e) {
                conn.rollback();
                Long uid = UserSession.getCurrentUser() != null ? UserSession.getCurrentUser().getId() : null;
                AppLogger.logError(HttpMethod.POST, "/reservations", e.getMessage(), 400, uid);
                throw e;
            }
        } catch (SQLException e) {
            throw new ReservationException("Error creating reservation: " + e.getMessage(), e);
        }
    }

    /**
     * Executes check-out for a reservation.
     */
    public void executeCheckout(Long reservationId) {
        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Reservation reservation = reservationDao.findById(conn, reservationId)
                        .orElseThrow(() -> new ReservationException("Reservation not found"));

                // BR-005: Must be CHECKIN status
                if (reservation.getStatus() != ReservationStatus.CHECKIN) {
                    throw new ReservationException("There is no active check-in for this reservation");
                }

                Room room = roomDao.findById(conn, reservation.getRoomId())
                        .orElseThrow(() -> new ReservationException("Room not found"));

                // Cost calculation
                double totalCost = calculateTotalCost(reservation, room.getPricePerNight());
                
                // Finalize reservation
                reservationDao.finish(conn, reservationId, totalCost);

                // Update room to AVAILABLE
                roomDao.updateStatus(conn, reservation.getRoomId(), RoomStatus.AVAILABLE);

                conn.commit();
                Long uid = UserSession.getCurrentUser() != null ? UserSession.getCurrentUser().getId() : null;
                AppLogger.log(HttpMethod.PATCH, "/reservations/" + reservationId,
                    "Check-out completed for reservation " + reservationId, 200, uid);
            } catch (Exception e) {
                conn.rollback();
                Long uid = UserSession.getCurrentUser() != null ? UserSession.getCurrentUser().getId() : null;
                AppLogger.logError(HttpMethod.PATCH, "/reservations/" + reservationId, e.getMessage(), 400, uid);
                throw e;
            }
        } catch (SQLException e) {
            throw new ReservationException("Error executing check-out: " + e.getMessage(), e);
        }
    }

    private double calculateTotalCost(Reservation r, double pricePerNight) {
        // CALC-001: Nights
        long nights = ChronoUnit.DAYS.between(r.getCheckIn(), r.getCheckOut());
        if (nights <= 0) nights = 1; // Minimum 1 night if for some reason dates are same

        // CALC-002: Subtotal
        double subtotal = nights * pricePerNight;

        // CALC-003: Total with VAT
        double vat = ConfigUtil.getVat();
        return subtotal * (1 + vat);
    }

    public List<Reservation> listActive() {
        try (Connection conn = ConnectionFactory.getConnection()) {
            return reservationDao.listActive(conn);
        } catch (SQLException e) {
            throw new ReservationException("Error listing reservations", e);
        }
    }
}
