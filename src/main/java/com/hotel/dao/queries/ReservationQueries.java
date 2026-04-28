package com.hotel.dao.queries;

/**
 * SQL Queries for the reservations table.
 */
public class ReservationQueries {

    public static final String INSERT = 
        "INSERT INTO reservations (room_id, guest_id, user_id, checkin_date, checkout_date, status, total_cost, created_at) " +
        "VALUES (?, ?, ?, ?, ?, 'CHECKIN'::reservation_status, 0, NOW())";

    public static final String UPDATE = 
        "UPDATE reservations SET room_id = ?, guest_id = ?, user_id = ?, checkin_date = ?, checkout_date = ?, status = ?::reservation_status, total_cost = ? " +
        "WHERE id = ?";

    public static final String FIND_BY_ID = 
        "SELECT * FROM reservations WHERE id = ?";

    public static final String LIST_ALL = 
        "SELECT r.*, rm.number as room_number, (g.first_name || ' ' || g.last_name) as guest_name " +
        "FROM reservations r " +
        "JOIN rooms rm ON r.room_id = rm.id " +
        "JOIN guests g ON r.guest_id = g.id";

    public static final String LIST_ACTIVE = 
        "SELECT r.*, rm.number as room_number, (g.first_name || ' ' || g.last_name) as guest_name " +
        "FROM reservations r " +
        "JOIN rooms rm ON r.room_id = rm.id " +
        "JOIN guests g ON r.guest_id = g.id " +
        "WHERE r.status IN ('ACTIVE', 'CHECKIN')";

    public static final String LIST_BY_GUEST = 
        "SELECT * FROM reservations WHERE guest_id = ?";

    public static final String CHECK_OVERLAP = 
        "SELECT COUNT(*) FROM reservations " +
        "WHERE room_id = ? " +
        "AND status NOT IN ('FINISHED', 'CANCELLED') " +
        "AND checkin_date < ? AND checkout_date > ?";

    public static final String FIND_BY_ROOM_AND_STATUS = 
        "SELECT * FROM reservations WHERE room_id = ? AND status = ?";

    public static final String FINISH = 
        "UPDATE reservations SET status = 'FINISHED'::reservation_status, total_cost = ? WHERE id = ?";

    public static final String DELETE = 
        "DELETE FROM reservations WHERE id = ?";
}
