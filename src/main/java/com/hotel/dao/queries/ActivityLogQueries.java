package com.hotel.dao.queries;

/**
 * SQL queries for the activity_logs table.
 */
public class ActivityLogQueries {

    public static final String INSERT =
        "INSERT INTO activity_logs (user_id, http_method, resource, description, status_code, created_at) " +
        "VALUES (?, ?::http_method, ?, ?, ?, ?)";

    private ActivityLogQueries() {}
}
