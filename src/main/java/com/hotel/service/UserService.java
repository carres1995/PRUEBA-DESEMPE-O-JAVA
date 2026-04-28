package com.hotel.service;

import com.hotel.config.ConnectionFactory;
import com.hotel.dao.interfaces.IUserDao;
import com.hotel.exception.UserException;
import com.hotel.model.User;
import com.hotel.util.PasswordHasher;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service layer for User management.
 */
public class UserService {

    private final IUserDao userDao;

    public UserService(IUserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Registers a new user.
     * 
     * @param user The user to register
     * @param plainPassword The plain text password to hash
     */
    public User register(User user, String plainPassword) {
        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // BR-001: Username required
                if (user.getUsername() == null || user.getUsername().isBlank()) {
                    throw new UserException("Username is required");
                }

                // BR-002: Password length
                if (plainPassword == null || plainPassword.length() < 8) {
                    throw new UserException("Password must be at least 8 characters long");
                }

                // BR-003: Unique username
                if (userDao.existsByUsername(conn, user.getUsername())) {
                    throw new UserException("Username is already registered");
                }

                // New Validation: Email required and format
                if (user.getEmail() == null || user.getEmail().isBlank()) {
                    throw new UserException("Email is required");
                }
                if (!user.getEmail().contains("@")) {
                    throw new UserException("Invalid email format");
                }

                // BR-004: Role validation
                if (user.getRole() == null) {
                    throw new UserException("The specified role is invalid");
                }

                // BR-005: BCrypt hashing
                user.setPasswordHash(PasswordHasher.hash(plainPassword));
                
                user.setActive(true);
                user.setCreatedAt(LocalDateTime.now());

                userDao.insert(conn, user);

                conn.commit();
                return user;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new UserException("Error registering user: " + e.getMessage(), e);
        }
    }

    public List<User> listAll() {
        try (Connection conn = ConnectionFactory.getConnection()) {
            return userDao.listAll(conn);
        } catch (SQLException e) {
            throw new UserException("Error listing users", e);
        }
    }

    public void toggleActive(Long id) {
        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);
            try {
                User user = userDao.findById(conn, id)
                        .orElseThrow(() -> new UserException("User not found"));

                user.setActive(!user.isActive());
                userDao.update(conn, user);

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new UserException("Error toggling user status", e);
        }
    }

}
