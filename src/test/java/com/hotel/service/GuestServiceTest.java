package com.hotel.service;

import com.hotel.dao.interfaces.IGuestDao;
import com.hotel.exception.GuestException;
import com.hotel.model.Guest;
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
@DisplayName("GuestService — Business Rule Tests")
class GuestServiceTest {

    @Mock
    private IGuestDao guestDao;

    @InjectMocks
    private GuestService guestService;

    // Helper for lambdas
    private Guest callRegister(Guest g) throws SQLException {
        return guestService.register(g);
    }

    private Guest callUpdate(Guest g) throws SQLException {
        return guestService.update(g);
    }

    private void callDeactivate(Guest g) throws SQLException {
        guestService.deactivate(g);
    }

    // =====================================================================
    // SCENARIO 1: Happy Path - Successful Registration
    // =====================================================================
    @Test
    @DisplayName("Scenario 1: Should successfully register a guest")
    void shouldRegisterGuestSuccessfully() throws Exception {
        Guest guest = new Guest();
        guest.setFirstName("Carlos");
        guest.setLastName("Ruiz");
        guest.setDocumentNumber("12345678");
        guest.setEmail("carlos@mail.com");
        guest.setPhone("3001234567");

        try {
            doReturn(false).when(guestDao).existsByDocument(any(), eq("12345678"));
            doNothing().when(guestDao).insert(any(), any());
        } catch (SQLException e) {
            fail("Mock setup failed");
        }

        Guest result = guestService.register(guest);

        assertNotNull(result);
        assertNotNull(result.getCreatedAt());
        assertTrue(result.isActive());
    }

    // =====================================================================
    // SCENARIO 2: Empty First Name
    // =====================================================================
    @Test
    @DisplayName("Scenario 2: Should throw exception when firstName is null")
    void shouldThrowExceptionWhenFirstNameIsEmpty() throws Exception {
        Guest guest = new Guest();
        guest.setFirstName(null);

        GuestException thrown = assertThrows(GuestException.class, () -> callRegister(guest));
        assertEquals("Guest first name is required", thrown.getMessage());
    }

    // =====================================================================
    // SCENARIO 3: Empty Last Name
    // =====================================================================
    @Test
    @DisplayName("Scenario 3: Should throw exception when lastName is null")
    void shouldThrowExceptionWhenLastNameIsEmpty() throws Exception {
        Guest guest = new Guest();
        guest.setFirstName("Carlos");
        guest.setLastName("");

        GuestException thrown = assertThrows(GuestException.class, () -> callRegister(guest));
        assertEquals("Guest last name is required", thrown.getMessage());
    }

    // =====================================================================
    // SCENARIO 4: Duplicate Document
    // =====================================================================
    @Test
    @DisplayName("Scenario 4: Should throw exception when document already exists")
    void shouldThrowExceptionWhenDocumentIsDuplicate() throws Exception {
        Guest guest = new Guest();
        guest.setFirstName("Carlos");
        guest.setLastName("Ruiz");
        guest.setDocumentNumber("12345678");

        try {
            doReturn(true).when(guestDao).existsByDocument(any(), eq("12345678"));
        } catch (SQLException e) {
            fail("Mock setup failed");
        }

        GuestException thrown = assertThrows(GuestException.class, () -> callRegister(guest));
        assertEquals("A guest with that document number already exists", thrown.getMessage());
    }

    // =====================================================================
    // SCENARIO 5: Invalid Email
    // =====================================================================
    @Test
    @DisplayName("Scenario 5: Should throw exception when email format is invalid")
    void shouldThrowExceptionWhenEmailIsInvalid() throws Exception {
        Guest guest = new Guest();
        guest.setFirstName("Carlos");
        guest.setLastName("Ruiz");
        guest.setDocumentNumber("12345678");
        guest.setEmail("invalid-email");

        GuestException thrown = assertThrows(GuestException.class, () -> callRegister(guest));
        assertEquals("The email format is invalid", thrown.getMessage());
    }

    // =====================================================================
    // SCENARIO 6: Invalid Phone
    // =====================================================================
    @Test
    @DisplayName("Scenario 6: Should throw exception when phone contains letters")
    void shouldThrowExceptionWhenPhoneIsInvalid() throws Exception {
        Guest guest = new Guest();
        guest.setFirstName("Carlos");
        guest.setLastName("Ruiz");
        guest.setDocumentNumber("12345678");
        guest.setPhone("ABC123XYZ");

        GuestException thrown = assertThrows(GuestException.class, () -> callRegister(guest));
        assertEquals("The phone must contain between 7 and 15 digits", thrown.getMessage());
    }

    // =====================================================================
    // SCENARIO 7: Deactivate with Active Reservations
    // =====================================================================
    @Test
    @DisplayName("Scenario 7: Should throw exception when deactivating guest with active reservations")
    void shouldThrowExceptionWhenDeactivatingWithReservations() throws Exception {
        Guest guest = new Guest();
        guest.setId(1L);

        try {
            doReturn(2).when(guestDao).countActiveReservations(any(), eq(1L));
        } catch (SQLException e) {
            fail("Mock setup failed");
        }

        GuestException thrown = assertThrows(GuestException.class, () -> callDeactivate(guest));
        assertEquals("Cannot deactivate a guest with active reservations", thrown.getMessage());
    }

    // =====================================================================
    // SCENARIO 8: Successful Update
    // =====================================================================
    @Test
    @DisplayName("Scenario 8: Should update guest successfully")
    void shouldUpdateGuestSuccessfully() throws Exception {
        Guest guest = new Guest();
        guest.setId(5L);
        guest.setFirstName("Carlos");
        guest.setLastName("Ruiz");
        guest.setDocumentNumber("12345678");
        guest.setEmail("new@mail.com");

        try {
            doReturn(Optional.of(guest)).when(guestDao).findById(any(), eq(5L));
            doNothing().when(guestDao).update(any(), any());
        } catch (SQLException e) {
            fail("Mock setup failed");
        }

        Guest result = guestService.update(guest);
        assertEquals("new@mail.com", result.getEmail());
    }

    // =====================================================================
    // SCENARIO 9: Boundary Value - 7 digit phone
    // =====================================================================
    @Test
    @DisplayName("Scenario 9: Should register successfully with 7 digit phone")
    void shouldRegisterWith7DigitPhone() throws Exception {
        Guest guest = new Guest();
        guest.setFirstName("Carlos");
        guest.setLastName("Ruiz");
        guest.setDocumentNumber("12345678");
        guest.setPhone("1234567");

        try {
            doReturn(false).when(guestDao).existsByDocument(any(), eq("12345678"));
            doNothing().when(guestDao).insert(any(), any());
        } catch (SQLException e) {
            fail("Mock setup failed");
        }

        Guest result = guestService.register(guest);
        assertNotNull(result);
    }

    // =====================================================================
    // SCENARIO 10: Allowed Deactivation
    // =====================================================================
    @Test
    @DisplayName("Scenario 10: Should allow deactivation when no active reservations")
    void shouldAllowDeactivation() throws Exception {
        Guest guest = new Guest();
        guest.setId(1L);

        try {
            doReturn(0).when(guestDao).countActiveReservations(any(), eq(1L));
            doNothing().when(guestDao).update(any(), any());
        } catch (SQLException e) {
            fail("Mock setup failed");
        }

        guestService.deactivate(guest);
        assertFalse(guest.isActive());
    }

    // =====================================================================
    // SCENARIO 10.1: BR-006 — Cannot deactivate with active reservations
    // =====================================================================
    @Test
    @DisplayName("Scenario 10.1 (BR-006): Should throw exception when deactivating guest with active reservations")
    void shouldThrowWhenDeactivatingGuestWithActiveReservations() throws Exception {
        Guest guest = new Guest();
        guest.setId(5L);
        guest.setActive(true);

        try {
            doReturn(2).when(guestDao).countActiveReservations(any(), eq(5L));
        } catch (SQLException e) {
            fail("Mock setup failed");
        }

        GuestException thrown = assertThrows(GuestException.class, () -> guestService.deactivate(guest));
        assertEquals("Cannot deactivate a guest with active reservations", thrown.getMessage());
        assertTrue(guest.isActive(), "Guest should remain active after failed deactivation");
    }
}
