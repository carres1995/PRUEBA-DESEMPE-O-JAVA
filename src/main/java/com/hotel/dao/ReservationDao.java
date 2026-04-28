package com.hotel.dao;

import com.hotel.model.Reservation;
import com.hotel.model.enums.ReservationStatus;
import com.hotel.dao.queries.ReservationQueries;
import com.hotel.dao.interfaces.IReservationDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of IReservationDao using pure JDBC.
 */
public class ReservationDao implements IReservationDao {

    @Override
    public void insert(Connection conn, Reservation reservation) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(ReservationQueries.INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, reservation.getRoomId());
            ps.setLong(2, reservation.getGuestId());
            ps.setLong(3, reservation.getUserId());
            ps.setDate(4, Date.valueOf(reservation.getCheckIn()));
            ps.setDate(5, Date.valueOf(reservation.getCheckOut()));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    reservation.setId(rs.getLong(1));
                }
            }
        }
    }

    @Override
    public void update(Connection conn, Reservation reservation) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(ReservationQueries.UPDATE)) {
            ps.setLong(1, reservation.getRoomId());
            ps.setLong(2, reservation.getGuestId());
            ps.setLong(3, reservation.getUserId());
            ps.setDate(4, Date.valueOf(reservation.getCheckIn()));
            ps.setDate(5, Date.valueOf(reservation.getCheckOut()));
            ps.setString(6, reservation.getStatus().name());
            ps.setDouble(7, reservation.getTotalCost());
            ps.setLong(8, reservation.getId());

            ps.executeUpdate();
        }
    }

    @Override
    public Optional<Reservation> findById(Connection conn, Long id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(ReservationQueries.FIND_BY_ID)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Reservation> listAll(Connection conn) throws SQLException {
        List<Reservation> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(ReservationQueries.LIST_ALL)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    @Override
    public void delete(Connection conn, Long id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(ReservationQueries.DELETE)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Reservation> listActive(Connection conn) throws SQLException {
        List<Reservation> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(ReservationQueries.LIST_ACTIVE)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<Reservation> listByGuest(Connection conn, Long guestId) throws SQLException {
        List<Reservation> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(ReservationQueries.LIST_BY_GUEST)) {
            ps.setLong(1, guestId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    @Override
    public boolean hasOverlap(Connection conn, Long roomId, java.time.LocalDate checkIn, java.time.LocalDate checkOut) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(ReservationQueries.CHECK_OVERLAP)) {
            ps.setLong(1, roomId);
            ps.setDate(2, Date.valueOf(checkOut));
            ps.setDate(3, Date.valueOf(checkIn));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    @Override
    public Optional<Reservation> findByRoomAndStatus(Connection conn, Long roomId, ReservationStatus status) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(ReservationQueries.FIND_BY_ROOM_AND_STATUS)) {
            ps.setLong(1, roomId);
            ps.setString(2, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void finish(Connection conn, Long reservationId, double totalCost) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(ReservationQueries.FINISH)) {
            ps.setDouble(1, totalCost);
            ps.setLong(2, reservationId);
            ps.executeUpdate();
        }
    }

    private Reservation mapRow(ResultSet rs) throws SQLException {
        Reservation reservation = new Reservation();
        reservation.setId(rs.getLong("id"));
        reservation.setRoomId(rs.getLong("room_id"));
        reservation.setGuestId(rs.getLong("guest_id"));
        reservation.setUserId(rs.getLong("user_id"));
        reservation.setCheckIn(rs.getDate("checkin_date").toLocalDate());
        reservation.setCheckOut(rs.getDate("checkout_date").toLocalDate());
        reservation.setStatus(ReservationStatus.valueOf(rs.getString("status")));
        reservation.setTotalCost(rs.getDouble("total_cost"));
        reservation.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

        // Optional columns from JOINs
        try {
            reservation.setRoomNumber(rs.getString("room_number"));
            reservation.setGuestName(rs.getString("guest_name"));
        } catch (SQLException e) {
            // These columns might not exist in all queries (like findById if not updated)
        }

        return reservation;
    }
}
