package com.hotel.dao;

import com.hotel.dao.interfaces.IGenericDao;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Base implementation for DAOs providing common CRUD operations.
 */
public abstract class AbstractDao<T, K> implements IGenericDao<T, K> {

    protected final String tableName;

    protected AbstractDao(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public Optional<T> findById(Connection conn, K id) throws SQLException {
        String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<T> listAll(Connection conn) throws SQLException {
        List<T> list = new ArrayList<>();
        String sql = "SELECT * FROM " + tableName;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public void delete(Connection conn, K id) throws SQLException {
        String sql = "DELETE FROM " + tableName + " WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Maps a single row from a ResultSet to an entity.
     * Must be implemented by concrete DAOs.
     */
    protected abstract T mapRow(ResultSet rs) throws SQLException;
}
