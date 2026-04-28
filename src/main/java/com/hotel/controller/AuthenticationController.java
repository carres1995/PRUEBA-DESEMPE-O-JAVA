package com.hotel.controller;

import com.hotel.model.User;
import com.hotel.service.AuthenticationService;
import com.hotel.util.UserSession;

/**
 * Controller for authentication operations.
 */
public class AuthenticationController {

    private final AuthenticationService authService;

    public AuthenticationController(AuthenticationService authService) {
        this.authService = authService;
    }

    /**
     * Attempts to login a user.
     * 
     * @param username The username
     * @param password The password
     * @return true if successful, false otherwise
     */
    public void login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new RuntimeException("Username and password are required");
        }
        
        User user = authService.login(username, password);
        UserSession.login(user);
    }

    public void logout() {
        UserSession.logout();
    }
}
