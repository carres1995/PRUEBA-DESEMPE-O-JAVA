package com.hotel.controller;

import com.hotel.model.Room;
import com.hotel.model.enums.RoomStatus;
import com.hotel.model.enums.RoomType;
import com.hotel.service.RoomService;

import java.util.List;

/**
 * Controller for Room management.
 * 
 * <p>Receives user input from the View, performs basic data conversion,
 * and delegates to the Service layer.</p>
 */
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    /**
     * Handles room registration.
     */
    public void handleSave(String number, String typeStr, String capacityStr, String priceStr) {
        Room room = new Room();
        room.setNumber(number);
        
        try {
            if (typeStr != null) {
                room.setType(RoomType.valueOf(typeStr.toUpperCase()));
            }
            if (capacityStr != null && !capacityStr.isBlank()) {
                room.setCapacity(Integer.parseInt(capacityStr));
            }
            if (priceStr != null && !priceStr.isBlank()) {
                room.setPricePerNight(Double.parseDouble(priceStr));
            }
        } catch (IllegalArgumentException e) {
            // Rethrow as business exception for the View to catch
            throw new RuntimeException("Invalid format for numeric or type fields: " + e.getMessage());
        }

        roomService.register(room);
    }

    /**
     * Handles room update.
     */
    public void handleUpdate(Long id, String number, String typeStr, String capacityStr, String priceStr, String statusStr, boolean isActive) {
        Room room = new Room();
        room.setId(id);
        room.setNumber(number);
        room.setActive(isActive);

        try {
            if (typeStr != null) {
                room.setType(RoomType.valueOf(typeStr.toUpperCase()));
            }
            if (capacityStr != null && !capacityStr.isBlank()) {
                room.setCapacity(Integer.parseInt(capacityStr));
            }
            if (priceStr != null && !priceStr.isBlank()) {
                room.setPricePerNight(Double.parseDouble(priceStr));
            }
            if (statusStr != null) {
                room.setStatus(RoomStatus.valueOf(statusStr.toUpperCase()));
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid format for numeric or type fields: " + e.getMessage());
        }

        roomService.update(room);
    }

    /**
     * Toggles room active state.
     */
    public void handleToggleActive(Room room) {
        if (room.isActive()) {
            roomService.deactivate(room);
        } else {
            room.setActive(true);
            roomService.update(room);
        }
    }

    /**
     * Lists rooms by filter.
     */
    public List<Room> listByTypeAndStatus(RoomType type, RoomStatus status) {
        return roomService.listByTypeAndStatus(type, status);
    }

    public List<String> listAvailableNumbers() {
        return roomService.listAvailableNumbers();
    }
}
