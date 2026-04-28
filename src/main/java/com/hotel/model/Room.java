package com.hotel.model;

import com.hotel.model.enums.RoomStatus;
import com.hotel.model.enums.RoomType;

import java.time.LocalDateTime;
import java.util.Objects;

public class Room {
    private Long id;
    private String number;
    private RoomType type;
    private int capacity;
    private double pricePerNight;
    private RoomStatus status;
    private boolean isActive;
    private LocalDateTime createdAt;

    public Room() {
    }

    public Room(Long id, String number, RoomType type, int capacity, double pricePerNight, RoomStatus status, boolean isActive, LocalDateTime createdAt) {
        this.id = id;
        this.number = number;
        this.type = type;
        this.capacity = capacity;
        this.pricePerNight = pricePerNight;
        this.status = status;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public RoomType getType() {
        return type;
    }

    public void setType(RoomType type) {
        this.type = type;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return capacity == room.capacity &&
                Double.compare(room.pricePerNight, pricePerNight) == 0 &&
                isActive == room.isActive &&
                Objects.equals(id, room.id) &&
                Objects.equals(number, room.number) &&
                type == room.type &&
                status == room.status &&
                Objects.equals(createdAt, room.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, number, type, capacity, pricePerNight, status, isActive, createdAt);
    }

    @Override
    public String toString() {
        return "Room{" +
                "id=" + id +
                ", number='" + number + "'" +
                ", type=" + type +
                ", capacity=" + capacity +
                ", pricePerNight=" + pricePerNight +
                ", status=" + status +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
}
