package com.hotel.dao.queries;

public class RoomQueries {
    public static final String INSERT = 
        "INSERT INTO rooms (number, type, capacity, price_per_night, status, is_active, created_at) VALUES (?, ?::room_type, ?, ?, ?::room_status, ?, ?)";
    
    public static final String UPDATE = 
        "UPDATE rooms SET number = ?, type = ?::room_type, capacity = ?, price_per_night = ?, status = ?::room_status, is_active = ? WHERE id = ?";
    
    public static final String FIND_BY_ID = 
        "SELECT id, number, type, capacity, price_per_night, status, is_active, created_at FROM rooms WHERE id = ?";
    
    public static final String EXISTS_BY_NUMBER = 
        "SELECT COUNT(*) FROM rooms WHERE number = ?";
    
    public static final String EXISTS_BY_NUMBER_NOT_ID = 
        "SELECT COUNT(*) FROM rooms WHERE number = ? AND id <> ?";
    

    public static final String LIST_ALL = 
        "SELECT id, number, type, capacity, price_per_night, status, is_active, created_at FROM rooms";

    public static final String BASE_QUERY = 
        "SELECT id, number, type, capacity, price_per_night, status, is_active, created_at FROM rooms WHERE 1=1";

    public static final String LIST_AVAILABLE_NUMBERS = 
        "SELECT room_number FROM room_inventory WHERE room_number NOT IN (SELECT number FROM rooms)";
}
