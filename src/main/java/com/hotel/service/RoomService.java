package com.hotel.service;

import com.hotel.config.ConnectionFactory;
import com.hotel.dao.interfaces.IRoomDao;
import com.hotel.exception.RoomException;
import com.hotel.model.Room;
import com.hotel.model.enums.RoomStatus;
import com.hotel.model.enums.RoomType;
import com.hotel.model.enums.HttpMethod;
import com.hotel.util.AppLogger;
import com.hotel.util.UserSession;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service layer for Room management.
 * 
 * <p>Handles business rules and transaction management.</p>
 */
public class RoomService {

    private final IRoomDao roomDao;

    public RoomService(IRoomDao roomDao) {
        this.roomDao = roomDao;
    }

    /**
     * Registers a new room.
     */
    public Room register(Room room) {
        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // === BUSINESS RULE VALIDATIONS ===
                validateRoom(conn, room, true);

                // Default values
                room.setStatus(RoomStatus.AVAILABLE);
                room.setActive(true);
                room.setCreatedAt(LocalDateTime.now());

                roomDao.insert(conn, room);

                conn.commit();
                Long uid = UserSession.getCurrentUser() != null ? UserSession.getCurrentUser().getId() : null;
                AppLogger.log(HttpMethod.POST, "/rooms", "Room " + room.getNumber() + " registered", 201, uid);
                return room;
            } catch (Exception e) {
                conn.rollback();
                Long uid = UserSession.getCurrentUser() != null ? UserSession.getCurrentUser().getId() : null;
                AppLogger.logError(HttpMethod.POST, "/rooms", e.getMessage(), 400, uid);
                throw e;
            }
        } catch (SQLException e) {
            throw new RoomException("Error registering room: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing room.
     */
    public Room update(Room room) {
        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (room.getId() == null) {
                    throw new RoomException("Room ID is required for update");
                }

                validateRoom(conn, room, false);

                roomDao.update(conn, room);

                conn.commit();
                Long uid = UserSession.getCurrentUser() != null ? UserSession.getCurrentUser().getId() : null;
                AppLogger.log(HttpMethod.PUT, "/rooms/" + room.getId(), "Room " + room.getNumber() + " updated", 200, uid);
                return room;
            } catch (Exception e) {
                conn.rollback();
                Long uid = UserSession.getCurrentUser() != null ? UserSession.getCurrentUser().getId() : null;
                AppLogger.logError(HttpMethod.PUT, "/rooms/" + room.getId(), e.getMessage(), 400, uid);
                throw e;
            }
        } catch (SQLException e) {
            throw new RoomException("Error updating room: " + e.getMessage(), e);
        }
    }

    /**
     * Deactivates a room.
     */
    public void deactivate(Room room) {
        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // BR-006: Cannot deactivate occupied room
                if (room.getStatus() == RoomStatus.OCCUPIED) {
                    throw new RoomException("Cannot deactivate a room that is occupied");
                }

                room.setActive(false);
                roomDao.update(conn, room);

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RoomException("Error deactivating room: " + e.getMessage(), e);
        }
    }

    /**
     * Filters rooms by type and status.
     */
    public List<Room> listByTypeAndStatus(RoomType type, RoomStatus status) {
        try (Connection conn = ConnectionFactory.getConnection()) {
            if (type == null && status == null) {
                return roomDao.listAll(conn);
            }
            return roomDao.listByTypeAndStatus(conn, type, status);
        } catch (SQLException e) {
            throw new RoomException("Error listing rooms: " + e.getMessage(), e);
        }
    }

    /**
     * Lists room numbers available in the physical inventory but not registered.
     */
    public List<String> listAvailableNumbers() {
        try (Connection conn = ConnectionFactory.getConnection()) {
            return roomDao.listAvailableNumbers(conn);
        } catch (SQLException e) {
            throw new RoomException("Error loading available numbers: " + e.getMessage(), e);
        }
    }

    // === PRIVATE HELPERS ===

    private void validateRoom(Connection conn, Room room, boolean isNew) throws SQLException {
        // BR-001: Room number required
        if (room.getNumber() == null || room.getNumber().isBlank()) {
            throw new RoomException("Room number is required");
        }

        // BR-003: Unique room number
        boolean exists = isNew ? roomDao.existsByNumber(conn, room.getNumber()) 
                               : roomDao.existsByNumberAndNotId(conn, room.getNumber(), room.getId());
        if (exists) {
            throw new RoomException("A room with that number already exists");
        }

        // BR-005: Room type required
        if (room.getType() == null) {
            throw new RoomException("Room type is invalid");
        }

        // BR-004: Capacity > 0
        if (room.getCapacity() <= 0) {
            throw new RoomException("Capacity must be greater than zero");
        }

        // BR-002: Price > 0
        if (room.getPricePerNight() <= 0) {
            throw new RoomException("Price per night must be greater than zero");
        }
    }

}
