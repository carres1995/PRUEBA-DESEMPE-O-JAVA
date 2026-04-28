package com.hotel.util;

import com.hotel.model.User;
import com.hotel.model.enums.Role;

/**
 * Singleton to manage the current user session in the application.
 */
public class UserSession {

    private static User currentUser;

    private UserSession() {}

    public static void login(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public static boolean isAdmin() {
        return isLoggedIn() && currentUser.getRole() == Role.ADMIN;
    }
}
