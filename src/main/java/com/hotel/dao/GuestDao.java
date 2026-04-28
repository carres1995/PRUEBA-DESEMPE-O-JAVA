package com.hotel.dao;

import com.hotel.model.Guest;
import com.hotel.dao.queries.GuestQueries;
import com.hotel.dao.interfaces.IGuestDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GuestDao implements IGuestDao {

    @Override
    public void insert(Connection conn, Guest guest) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(GuestQueries.INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, guest.getFirstName());
            ps.setString(2, guest.getLastName());
            ps.setString(3, guest.getDocumentNumber());
            ps.setString(4, guest.getEmail());
            ps.setString(5, guest.getPhone());
            ps.setBoolean(6, guest.isActive());
            ps.setTimestamp(7, Timestamp.valueOf(guest.getCreatedAt()));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    guest.setId(rs.getLong(1));
                }
            }
        }
    }

    @Override
    public void update(Connection conn, Guest guest) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(GuestQueries.UPDATE)) {
            ps.setString(1, guest.getFirstName());
            ps.setString(2, guest.getLastName());
            ps.setString(3, guest.getDocumentNumber());
            ps.setString(4, guest.getEmail());
            ps.setString(5, guest.getPhone());
            ps.setBoolean(6, guest.isActive());
            ps.setLong(7, guest.getId());

            ps.executeUpdate();
        }
    }

    @Override
    public Optional<Guest> findById(Connection conn, Long id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(GuestQueries.FIND_BY_ID)) {
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
    public Optional<Guest> findByDocument(Connection conn, String documentNumber) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(GuestQueries.FIND_BY_DOCUMENT)) {
            ps.setString(1, documentNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean existsByDocument(Connection conn, String documentNumber) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(GuestQueries.EXISTS_BY_DOCUMENT)) {
            ps.setString(1, documentNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    @Override
    public List<Guest> listAll(Connection conn) throws SQLException {
        List<Guest> guests = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(GuestQueries.LIST_ALL)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    guests.add(mapRow(rs));
                }
            }
        }
        return guests;
    }

    @Override
    public List<Guest> listActive(Connection conn) throws SQLException {
        List<Guest> guests = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(GuestQueries.LIST_ACTIVE)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    guests.add(mapRow(rs));
                }
            }
        }
        return guests;
    }

    @Override
    public void delete(Connection conn, Long id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(GuestQueries.DELETE)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public int countActiveReservations(Connection conn, Long guestId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(GuestQueries.COUNT_ACTIVE_RESERVATIONS)) {
            ps.setLong(1, guestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private Guest mapRow(ResultSet rs) throws SQLException {
        Guest guest = new Guest();
        guest.setId(rs.getLong("id"));
        guest.setFirstName(rs.getString("first_name"));
        guest.setLastName(rs.getString("last_name"));
        guest.setDocumentNumber(rs.getString("document_number"));
        guest.setEmail(rs.getString("email"));
        guest.setPhone(rs.getString("phone"));
        guest.setActive(rs.getBoolean("is_active"));
        guest.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return guest;
    }
}
