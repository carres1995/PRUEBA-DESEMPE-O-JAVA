package com.hotel.service;

import com.hotel.config.ConnectionFactory;
import com.hotel.dao.interfaces.IUserDao;
import com.hotel.exception.AuthenticationException;
import com.hotel.model.User;
import com.hotel.model.enums.HttpMethod;
import com.hotel.util.AppLogger;
import com.hotel.util.PasswordHasher;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Service layer for User authentication.
 */
public class AuthenticationService {

    private final IUserDao userDao;

    public AuthenticationService(IUserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Authenticates a user by username and password.
     * 
     * @param username The username
     * @param password The plain text password
     * @return The authenticated User object
     */
    public User login(String username, String password) {
        try (Connection conn = ConnectionFactory.getConnection()) {
            
            // BR-007: Generic error for non-existent user or wrong password
            User user = userDao.findByUsername(conn, username)
                    .orElseThrow(() -> {
                        AppLogger.logError(HttpMethod.POST, "/auth/login", "User not found: " + username, 401, null);
                        return new AuthenticationException("Invalid credentials");
                    });

            // BR-006: Check if user is active
            if (!user.isActive()) {
                AppLogger.logError(HttpMethod.POST, "/auth/login", "Inactive user: " + username, 403, user.getId());
                throw new AuthenticationException("Inactive user. Contact the administrator");
            }

            // BR-007: Verify password
            if (!PasswordHasher.check(password, user.getPasswordHash())) {
                AppLogger.logError(HttpMethod.POST, "/auth/login", "Invalid password for: " + username, 401, user.getId());
                throw new AuthenticationException("Invalid credentials");
            }

            AppLogger.log(HttpMethod.POST, "/auth/login", "User logged in: " + username, 200, user.getId());
            return user;

        } catch (SQLException e) {
            throw new RuntimeException("Database error during authentication", e);
        }
    }
}
