package com.hotel.dao.interfaces;

import com.hotel.model.Guest;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface IGuestDao extends IGenericDao<Guest, Long> {
    Optional<Guest> findByDocument(Connection conn, String documentNumber) throws SQLException;
    boolean existsByDocument(Connection conn, String documentNumber) throws SQLException;
    List<Guest> listActive(Connection conn) throws SQLException;
    int countActiveReservations(Connection conn, Long guestId) throws SQLException;
}
