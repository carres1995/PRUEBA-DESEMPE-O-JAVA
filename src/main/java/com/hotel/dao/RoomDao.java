package com.hotel.dao;

import com.hotel.model.Room;
import com.hotel.model.enums.RoomStatus;
import com.hotel.model.enums.RoomType;
import com.hotel.dao.queries.RoomQueries;
import com.hotel.dao.interfaces.IRoomDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of IRoomDao using pure JDBC.
 */
public class RoomDao implements IRoomDao {

    @Override
    public void insert(Connection conn, Room room) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(RoomQueries.INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, room.getNumber());
            ps.setString(2, room.getType().name());
            ps.setInt(3, room.getCapacity());
            ps.setDouble(4, room.getPricePerNight());
            ps.setString(5, room.getStatus().name());
            ps.setBoolean(6, room.isActive());
            ps.setTimestamp(7, Timestamp.valueOf(room.getCreatedAt()));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    room.setId(rs.getLong(1));
                }
            }
        }
    }

    @Override
    public void update(Connection conn, Room room) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(RoomQueries.UPDATE)) {
            ps.setString(1, room.getNumber());
            ps.setString(2, room.getType().name());
            ps.setInt(3, room.getCapacity());
            ps.setDouble(4, room.getPricePerNight());
            ps.setString(5, room.getStatus().name());
            ps.setBoolean(6, room.isActive());
            ps.setLong(7, room.getId());

            ps.executeUpdate();
        }
    }

    @Override
    public Optional<Room> findById(Connection conn, Long id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(RoomQueries.FIND_BY_ID)) {
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
    public boolean existsByNumber(Connection conn, String number) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(RoomQueries.EXISTS_BY_NUMBER)) {
            ps.setString(1, number);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    @Override
    public boolean existsByNumberAndNotId(Connection conn, String number, Long id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(RoomQueries.EXISTS_BY_NUMBER_NOT_ID)) {
            ps.setString(1, number);
            ps.setLong(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    @Override
    public List<Room> listByTypeAndStatus(Connection conn, RoomType type, RoomStatus status) throws SQLException {
        List<Room> rooms = new ArrayList<>();
        StringBuilder sql = new StringBuilder(RoomQueries.BASE_QUERY);
        
        if (type != null) sql.append(" AND type = ?::room_type");
        if (status != null) sql.append(" AND status = ?::room_status");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIdx = 1;
            if (type != null) ps.setString(paramIdx++, type.name());
            if (status != null) ps.setString(paramIdx++, status.name());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rooms.add(mapRow(rs));
                }
            }
        }
        return rooms;
    }

    @Override
    public List<Room> listAll(Connection conn) throws SQLException {
        List<Room> rooms = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(RoomQueries.LIST_ALL)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rooms.add(mapRow(rs));
                }
            }
        }
        return rooms;
    }

    private Room mapRow(ResultSet rs) throws SQLException {
        Room room = new Room();
        room.setId(rs.getLong("id"));
        room.setNumber(rs.getString("number"));
        room.setType(RoomType.valueOf(rs.getString("type")));
        room.setCapacity(rs.getInt("capacity"));
        room.setPricePerNight(rs.getDouble("price_per_night"));
        room.setStatus(RoomStatus.valueOf(rs.getString("status")));
        room.setActive(rs.getBoolean("is_active"));
        room.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return room;
    }

    @Override
    public void delete(Connection conn, Long id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM rooms WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public void updateStatus(Connection conn, Long roomId, RoomStatus status) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE rooms SET status = ?::room_status WHERE id = ?")) {
            ps.setString(1, status.name());
            ps.setLong(2, roomId);
            ps.executeUpdate();
        }
    }

    @Override
    public List<String> listAvailableNumbers(Connection conn) throws SQLException {
        List<String> numbers = new java.util.ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(RoomQueries.LIST_AVAILABLE_NUMBERS);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                numbers.add(rs.getString("room_number"));
            }
        }
        return numbers;
    }
}
