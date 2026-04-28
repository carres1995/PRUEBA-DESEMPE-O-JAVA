package com.hotel.dao.queries;

public class UserQueries {
    public static final String INSERT = 
        "INSERT INTO users (username, email, password_hash, role, is_active, created_at) VALUES (?, ?, ?, ?::user_role, ?, ?)";
    
    public static final String UPDATE = 
        "UPDATE users SET username = ?, email = ?, password_hash = ?, role = ?::user_role, is_active = ? WHERE id = ?";
    
    public static final String FIND_BY_ID = 
        "SELECT id, username, email, password_hash, role, is_active, created_at FROM users WHERE id = ?";
    
    public static final String FIND_BY_USERNAME = 
        "SELECT id, username, email, password_hash, role, is_active, created_at FROM users WHERE username = ?";
    
    public static final String EXISTS_BY_USERNAME = 
        "SELECT COUNT(*) FROM users WHERE username = ?";
    
    public static final String LIST_ALL = 
        "SELECT id, username, email, password_hash, role, is_active, created_at FROM users";
    
    public static final String DELETE = 
        "DELETE FROM users WHERE id = ?";
}
