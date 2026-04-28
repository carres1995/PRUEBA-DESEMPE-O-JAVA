package com.hotel.dao.queries;

public class GuestQueries {
    public static final String INSERT = 
        "INSERT INTO guests (first_name, last_name, document_number, email, phone, is_active, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    public static final String UPDATE = 
        "UPDATE guests SET first_name = ?, last_name = ?, document_number = ?, email = ?, phone = ?, is_active = ? WHERE id = ?";
    
    public static final String FIND_BY_ID = 
        "SELECT id, first_name, last_name, document_number, email, phone, is_active, created_at FROM guests WHERE id = ?";
    
    public static final String FIND_BY_DOCUMENT = 
        "SELECT id, first_name, last_name, document_number, email, phone, is_active, created_at FROM guests WHERE document_number = ?";
    
    public static final String EXISTS_BY_DOCUMENT = 
        "SELECT COUNT(*) FROM guests WHERE document_number = ?";
    
    public static final String LIST_ALL = 
        "SELECT id, first_name, last_name, document_number, email, phone, is_active, created_at FROM guests";
    
    public static final String LIST_ACTIVE = 
        "SELECT id, first_name, last_name, document_number, email, phone, is_active, created_at FROM guests WHERE is_active = TRUE";
    
    public static final String DELETE = 
        "DELETE FROM guests WHERE id = ?";
    
    public static final String COUNT_ACTIVE_RESERVATIONS = 
        "SELECT COUNT(*) FROM reservations WHERE guest_id = ? AND status IN ('ACTIVE', 'CHECKIN')";
}
