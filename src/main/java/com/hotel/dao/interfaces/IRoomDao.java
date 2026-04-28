package com.hotel.dao.interfaces;

import com.hotel.model.Room;
import com.hotel.model.enums.RoomStatus;
import com.hotel.model.enums.RoomType;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Interface for Room Data Access.
 */
public interface IRoomDao extends IGenericDao<Room, Long> {
    boolean existsByNumber(Connection conn, String number) throws SQLException;
    boolean existsByNumberAndNotId(Connection conn, String number, Long id) throws SQLException;
    List<Room> listByTypeAndStatus(Connection conn, RoomType type, RoomStatus status) throws SQLException;
    void updateStatus(Connection conn, Long roomId, RoomStatus status) throws SQLException;
    List<String> listAvailableNumbers(Connection conn) throws SQLException;
}
