package com.hotel.dao.interfaces;

import com.hotel.model.Reservation;
import com.hotel.model.enums.ReservationStatus;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface IReservationDao extends IGenericDao<Reservation, Long> {
    List<Reservation> listActive(Connection conn) throws SQLException;
    List<Reservation> listByGuest(Connection conn, Long guestId) throws SQLException;
    boolean hasOverlap(Connection conn, Long roomId, java.time.LocalDate checkIn, java.time.LocalDate checkOut) throws SQLException;
    Optional<Reservation> findByRoomAndStatus(Connection conn, Long roomId, ReservationStatus status) throws SQLException;
    void finish(Connection conn, Long reservationId, double totalCost) throws SQLException;
}
