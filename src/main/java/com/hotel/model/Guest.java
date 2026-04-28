package com.hotel.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Guest {
    private Long id;
    private String firstName;
    private String lastName;
    private String documentNumber;
    private String email;
    private String phone;
    private boolean isActive;
    private LocalDateTime createdAt;

    public Guest() {
    }

    public Guest(Long id, String firstName, String lastName, String documentNumber, String email, String phone, boolean isActive, LocalDateTime createdAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.documentNumber = documentNumber;
        this.email = email;
        this.phone = phone;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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
        Guest guest = (Guest) o;
        return isActive == guest.isActive &&
                Objects.equals(id, guest.id) &&
                Objects.equals(firstName, guest.firstName) &&
                Objects.equals(lastName, guest.lastName) &&
                Objects.equals(documentNumber, guest.documentNumber) &&
                Objects.equals(email, guest.email) &&
                Objects.equals(phone, guest.phone) &&
                Objects.equals(createdAt, guest.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, documentNumber, email, phone, isActive, createdAt);
    }

    @Override
    public String toString() {
        return "Guest{" +
                "id=" + id +
                ", firstName='" + firstName + "'" +
                ", lastName='" + lastName + "'" +
                ", documentNumber='" + documentNumber + "'" +
                ", email='" + email + "'" +
                ", phone='" + phone + "'" +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
}
