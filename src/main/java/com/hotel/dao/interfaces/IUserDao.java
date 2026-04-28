package com.hotel.dao.interfaces;

import com.hotel.model.User;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface IUserDao extends IGenericDao<User, Long> {
    Optional<User> findByUsername(Connection conn, String username) throws SQLException;
    boolean existsByUsername(Connection conn, String username) throws SQLException;
}
