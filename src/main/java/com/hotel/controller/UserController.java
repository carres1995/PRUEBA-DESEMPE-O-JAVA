package com.hotel.controller;

import com.hotel.model.enums.Role;
import com.hotel.model.User;
import com.hotel.service.UserService;

import java.util.List;

/**
 * Controller for user management operations.
 */
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public void handleRegister(String username, String email, String password, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setRole(role);
        
        userService.register(user, password);
    }

    public List<User> listAll() {
        return userService.listAll();
    }

    public void handleToggleActive(Long id) {
        userService.toggleActive(id);
    }
}
