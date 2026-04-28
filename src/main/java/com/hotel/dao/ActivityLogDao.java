package com.hotel.dao;

import com.hotel.config.ConnectionFactory;
import com.hotel.dao.queries.ActivityLogQueries;
import com.hotel.model.ActivityLog;

import java.sql.*;

/**
 * DAO for persisting activity log entries to the {@code activity_logs} table.
 */
public class ActivityLogDao {

    /**
     * Inserts a log entry into the database using its own connection.
     * Intentionally fire-and-forget: errors are swallowed so logging
     * never interrupts core business operations.
     *
     * @param log the activity log to persist
     */
    public void insert(ActivityLog log) {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(ActivityLogQueries.INSERT)) {

            if (log.getUserId() != null) {
                ps.setLong(1, log.getUserId());
            } else {
                ps.setNull(1, Types.BIGINT);
            }
            ps.setString(2, log.getHttpMethod().name());
            ps.setString(3, log.getResource());
            ps.setString(4, log.getDescription());
            ps.setInt(5, log.getStatusCode());
            ps.setTimestamp(6, Timestamp.valueOf(log.getCreatedAt()));

            ps.executeUpdate();

        } catch (Exception e) {
            // Logging failure must never crash the application
            System.err.println("⚠️  [AppLogger] Failed to persist log: " + e.getMessage());
        }
    }
}
