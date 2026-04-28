package com.hotel.service;

import com.hotel.dao.interfaces.IUserDao;
import com.hotel.exception.AuthenticationException;
import com.hotel.exception.UserException;
import com.hotel.model.enums.Role;
import com.hotel.model.User;
import com.hotel.util.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService & AuthenticationService — Business Rule Tests")
class UserServiceTest {

    @Mock
    private IUserDao userDao;

    @InjectMocks
    private UserService userService;

    @InjectMocks
    private AuthenticationService authService;

    @BeforeEach
    void setUp() {
        // Services are already initialized by @InjectMocks with the mock userDao
    }

    // Helper methods for lambdas
    private User callRegister(User user, String pass) throws SQLException {
        return userService.register(user, pass);
    }

    private User callLogin(String user, String pass) throws SQLException {
        return authService.login(user, pass);
    }

    // =====================================================================
    // SCENARIO 1: Happy Path - Successful Registration
    // =====================================================================
    @Test
    @DisplayName("Scenario 1: Should successfully register an ADMIN user")
    void shouldRegisterAdminSuccessfully() throws Exception {
        User user = new User();
        user.setUsername("admin01");
        user.setEmail("admin@example.com");
        user.setRole(Role.ADMIN);

        try {
            doReturn(false).when(userDao).existsByUsername(any(), eq("admin01"));
            doNothing().when(userDao).insert(any(), any());
        } catch (SQLException e) {
            fail("Mock setup failed");
        }

        User result = userService.register(user, "Admin2024!");

        assertNotNull(result);
        assertNotNull(result.getPasswordHash());
        assertTrue(PasswordHasher.check("Admin2024!", result.getPasswordHash()));
        assertNotNull(result.getCreatedAt());
    }

    // =====================================================================
    // SCENARIO 2: Empty Username
    // =====================================================================
    @Test
    @DisplayName("Scenario 2: Should throw exception when username is null")
    void shouldThrowExceptionWhenUsernameIsEmpty() throws Exception {
        User user = new User();
        user.setUsername(null);

        UserException thrown = assertThrows(UserException.class, () -> callRegister(user, "password123"));
        assertEquals("Username is required", thrown.getMessage());
    }

    // =====================================================================
    // SCENARIO 3: Password Too Short
    // =====================================================================
    @Test
    @DisplayName("Scenario 3: Should throw exception when password < 8 characters")
    void shouldThrowExceptionWhenPasswordIsShort() throws Exception {
        User user = new User();
        user.setUsername("user01");
        user.setEmail("user@example.com");

        UserException thrown = assertThrows(UserException.class, () -> callRegister(user, "abc123"));
        assertEquals("Password must be at least 8 characters long", thrown.getMessage());
    }

    // =====================================================================
    // SCENARIO 4: Duplicate Username
    // =====================================================================
    @Test
    @DisplayName("Scenario 4: Should throw exception when username already exists")
    void shouldThrowExceptionWhenUsernameIsDuplicate() throws Exception {
        User user = new User();
        user.setUsername("admin01");
        user.setEmail("admin@example.com");

        try {
            doReturn(true).when(userDao).existsByUsername(any(), eq("admin01"));
        } catch (SQLException e) {
            fail("Mock setup failed");
        }

        UserException thrown = assertThrows(UserException.class, () -> callRegister(user, "password123"));
        assertEquals("Username is already registered", thrown.getMessage());
    }

    // =====================================================================
    // SCENARIO 5: Invalid Role
    // =====================================================================
    @Test
    @DisplayName("Scenario 5: Should throw exception when role is null")
    void shouldThrowExceptionWhenRoleIsInvalid() throws Exception {
        User user = new User();
        user.setUsername("user01");
        user.setEmail("user@example.com");
        user.setRole(null);

        UserException thrown = assertThrows(UserException.class, () -> callRegister(user, "password123"));
        assertEquals("The specified role is invalid", thrown.getMessage());
    }

    @Test
    @DisplayName("Scenario 5.1: Should throw exception when email is invalid")
    void shouldThrowExceptionWhenEmailIsInvalid() throws Exception {
        User user = new User();
        user.setUsername("user01");
        user.setEmail("invalid-email");
        user.setRole(Role.RECEPTIONIST);

        UserException thrown = assertThrows(UserException.class, () -> callRegister(user, "password123"));
        assertEquals("Invalid email format", thrown.getMessage());
    }

    // =====================================================================
    // SCENARIO 6: Successful Login
    // =====================================================================
    @Test
    @DisplayName("Scenario 6: Should login successfully with correct credentials")
    void shouldLoginSuccessfully() throws Exception {
        User storedUser = new User();
        storedUser.setUsername("recep01");
        storedUser.setPasswordHash(PasswordHasher.hash("password123"));
        storedUser.setRole(Role.RECEPTIONIST);
        storedUser.setActive(true);

        try {
            doReturn(Optional.of(storedUser)).when(userDao).findByUsername(any(), eq("recep01"));
        } catch (SQLException e) {
            fail("Mock setup failed");
        }

        User result = authService.login("recep01", "password123");

        assertNotNull(result);
        assertEquals("recep01", result.getUsername());
        assertEquals(Role.RECEPTIONIST, result.getRole());
    }

    // =====================================================================
    // SCENARIO 7: Inactive User Login
    // =====================================================================
    @Test
    @DisplayName("Scenario 7: Should throw exception when login with inactive user")
    void shouldThrowExceptionWhenUserIsInactive() throws Exception {
        User storedUser = new User();
        storedUser.setUsername("user01");
        storedUser.setActive(false);

        try {
            doReturn(Optional.of(storedUser)).when(userDao).findByUsername(any(), eq("user01"));
        } catch (SQLException e) {
            fail("Mock setup failed");
        }

        AuthenticationException thrown = assertThrows(AuthenticationException.class, () -> callLogin("user01", "pass"));
        assertEquals("Inactive user. Contact the administrator", thrown.getMessage());
    }

    // =====================================================================
    // SCENARIO 8: Incorrect Credentials
    // =====================================================================
    @Test
    @DisplayName("Scenario 8: Should throw generic exception for invalid credentials")
    void shouldThrowGenericExceptionForWrongPassword() throws Exception {
        User storedUser = new User();
        storedUser.setUsername("user01");
        storedUser.setPasswordHash(PasswordHasher.hash("correct_pass"));
        storedUser.setActive(true);

        try {
            doReturn(Optional.of(storedUser)).when(userDao).findByUsername(any(), eq("user01"));
        } catch (SQLException e) {
            fail("Mock setup failed");
        }

        AuthenticationException thrown = assertThrows(AuthenticationException.class, () -> callLogin("user01", "wrong_pass"));
        assertEquals("Invalid credentials", thrown.getMessage());
    }

    @Test
    @DisplayName("Scenario 8.1: Should throw same generic exception when user does not exist")
    void shouldThrowGenericExceptionWhenUserNotFound() throws Exception {
        try {
            doReturn(Optional.empty()).when(userDao).findByUsername(any(), eq("unknown"));
        } catch (SQLException e) {
            fail("Mock setup failed");
        }

        AuthenticationException thrown = assertThrows(AuthenticationException.class, () -> callLogin("unknown", "any_pass"));
        assertEquals("Invalid credentials", thrown.getMessage());
    }

    // =====================================================================
    // SCENARIO 9: Boundary Value - Password 8 chars
    // =====================================================================
    @Test
    @DisplayName("Scenario 9: Should register successfully with 8 character password")
    void shouldRegisterWith8CharPassword() throws Exception {
        User user = new User();
        user.setUsername("user01");
        user.setEmail("user@example.com");
        user.setRole(Role.RECEPTIONIST);

        try {
            doReturn(false).when(userDao).existsByUsername(any(), eq("user01"));
            doNothing().when(userDao).insert(any(), any());
        } catch (SQLException e) {
            fail("Mock setup failed");
        }

        User result = userService.register(user, "Ab123456");
        assertNotNull(result);
    }
}
