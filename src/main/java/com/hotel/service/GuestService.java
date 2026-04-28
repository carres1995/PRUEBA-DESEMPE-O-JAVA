package com.hotel.service;

import com.hotel.config.ConnectionFactory;
import com.hotel.dao.interfaces.IGuestDao;
import com.hotel.exception.GuestException;
import com.hotel.model.Guest;
import com.hotel.model.enums.HttpMethod;
import com.hotel.util.AppLogger;
import com.hotel.util.UserSession;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Service layer for Guest management.
 */
public class GuestService {

    private final IGuestDao guestDao;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{7,15}$");

    public GuestService(IGuestDao guestDao) {
        this.guestDao = guestDao;
    }

    public Guest register(Guest guest) {
        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);
            try {
                validateGuest(conn, guest, true);

                guest.setActive(true);
                guest.setCreatedAt(LocalDateTime.now());

                guestDao.insert(conn, guest);

                conn.commit();
                Long uid = UserSession.getCurrentUser() != null ? UserSession.getCurrentUser().getId() : null;
                AppLogger.log(HttpMethod.POST, "/guests",
                    "Guest " + guest.getFirstName() + " " + guest.getLastName() + " registered", 201, uid);
                return guest;
            } catch (Exception e) {
                conn.rollback();
                Long uid = UserSession.getCurrentUser() != null ? UserSession.getCurrentUser().getId() : null;
                AppLogger.logError(HttpMethod.POST, "/guests", e.getMessage(), 400, uid);
                throw e;
            }
        } catch (SQLException e) {
            throw new GuestException("Error registering guest: " + e.getMessage(), e);
        }
    }

    public Guest update(Guest guest) {
        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (guest.getId() == null) {
                    throw new GuestException("Guest ID is required for update");
                }

                validateGuest(conn, guest, false);

                guestDao.update(conn, guest);

                conn.commit();
                Long uid = UserSession.getCurrentUser() != null ? UserSession.getCurrentUser().getId() : null;
                AppLogger.log(HttpMethod.PUT, "/guests/" + guest.getId(),
                    "Guest " + guest.getFirstName() + " updated", 200, uid);
                return guest;
            } catch (Exception e) {
                conn.rollback();
                Long uid = UserSession.getCurrentUser() != null ? UserSession.getCurrentUser().getId() : null;
                AppLogger.logError(HttpMethod.PUT, "/guests/" + guest.getId(), e.getMessage(), 400, uid);
                throw e;
            }
        } catch (SQLException e) {
            throw new GuestException("Error updating guest: " + e.getMessage(), e);
        }
    }

    public void deactivate(Guest guest) {
        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // BR-006: Cannot deactivate with active reservations
                int activeRes = guestDao.countActiveReservations(conn, guest.getId());
                if (activeRes > 0) {
                    throw new GuestException("Cannot deactivate a guest with active reservations");
                }

                guest.setActive(false);
                guestDao.update(conn, guest);

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new GuestException("Error deactivating guest: " + e.getMessage(), e);
        }
    }

    public List<Guest> listAll() {
        try (Connection conn = ConnectionFactory.getConnection()) {
            return guestDao.listAll(conn);
        } catch (SQLException e) {
            throw new GuestException("Error listing guests", e);
        }
    }

    private void validateGuest(Connection conn, Guest guest, boolean isNew) throws SQLException {
        // BR-001: First Name
        if (guest.getFirstName() == null || guest.getFirstName().isBlank()) {
            throw new GuestException("Guest first name is required");
        }

        // BR-002: Last Name
        if (guest.getLastName() == null || guest.getLastName().isBlank()) {
            throw new GuestException("Guest last name is required");
        }

        // BR-003: Unique Document
        if (guest.getDocumentNumber() == null || guest.getDocumentNumber().isBlank()) {
            throw new GuestException("Document number is required");
        }
        
        if (isNew || isDocumentChanged(conn, guest)) {
            if (guestDao.existsByDocument(conn, guest.getDocumentNumber())) {
                throw new GuestException("A guest with that document number already exists");
            }
        }

        // BR-004: Email format
        if (guest.getEmail() != null && !guest.getEmail().isBlank()) {
            if (!EMAIL_PATTERN.matcher(guest.getEmail()).matches()) {
                throw new GuestException("The email format is invalid");
            }
        }

        // BR-005: Phone format
        if (guest.getPhone() != null && !guest.getPhone().isBlank()) {
            if (!PHONE_PATTERN.matcher(guest.getPhone()).matches()) {
                throw new GuestException("The phone must contain between 7 and 15 digits");
            }
        }
    }

    private boolean isDocumentChanged(Connection conn, Guest guest) throws SQLException {
        return guestDao.findById(conn, guest.getId())
                .map(old -> !old.getDocumentNumber().equals(guest.getDocumentNumber()))
                .orElse(true);
    }

}
