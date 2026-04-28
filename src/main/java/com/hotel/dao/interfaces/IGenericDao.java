package com.hotel.dao.interfaces;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Generic interface for Data Access Objects.
 * 
 * @param <T> The entity type
 * @param <K> The primary key type
 */
public interface IGenericDao<T, K> {

    void insert(Connection conn, T entity) throws SQLException;

    void update(Connection conn, T entity) throws SQLException;

    Optional<T> findById(Connection conn, K id) throws SQLException;

    List<T> listAll(Connection conn) throws SQLException;

    void delete(Connection conn, K id) throws SQLException;
}
