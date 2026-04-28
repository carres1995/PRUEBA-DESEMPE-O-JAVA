package com.hotel.service;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.hotel.model.Room;
import com.hotel.model.enums.RoomType;
import com.hotel.model.enums.RoomStatus;
import com.hotel.dao.RoomDao;
import com.hotel.exception.RoomException;

@ExtendWith(MockitoExtension.class)
public class RoomServiceTest {

    @Mock
    private RoomDao roomDao;

    @InjectMocks
    private RoomService roomService;

    @BeforeEach
    void setUp() throws Exception {
        // Mockito injects @Mock fields into @InjectMocks automatically.
    }

    // ─── Helper methods for service calls inside assertThrows ────────────
    private Room callRegister(Room room) throws SQLException {
        return roomService.register(room);
    }

    private Room callUpdate(Room room) throws SQLException {
        return roomService.update(room);
    }

    private void callDeactivate(Room room) throws SQLException {
        roomService.deactivate(room);
    }

    // =====================================================================
    // ARCHETYPE 1: HAPPY PATH
    // Maps to: Scenario 1 in SPEC
    // =====================================================================
    @Test
    @DisplayName("Scenario 1: Should successfully register a valid room — BR-001, BR-002, BR-003, BR-004, BR-005")
    void shouldRegisterRoomSuccessfully() throws Exception {
        // GIVEN
        Room room = new Room();
        room.setNumber("101");
        room.setType(RoomType.DOUBLE);
        room.setCapacity(2);
        room.setPricePerNight(150000.0);
        room.setStatus(RoomStatus.AVAILABLE);
        room.setActive(true);

        try {
            doReturn(false).when(roomDao).existsByNumber(any(), eq("101")); // For BR-003 unique validation
            doNothing().when(roomDao).insert(any(), any());
        } catch (SQLException e) {
            fail("Mock setup should not throw");
        }

        // WHEN
        Room result = roomService.register(room);

        // THEN
        assertNotNull(result, "Registered room should not be null");
        assertNotNull(result.getCreatedAt(), "createdAt should be assigned automatically");
    }

    // =====================================================================
    // ARCHETYPE 2: NULL/EMPTY VALIDATION
    // Maps to: Scenario 2 in SPEC
    // =====================================================================
    @Test
    @DisplayName("Scenario 2: Should throw exception when room number is empty — BR-001")
    void shouldThrowExceptionWhenRoomNumberIsEmpty() throws Exception {
        // GIVEN
        Room room = new Room();
        room.setNumber(null); // Invalid: null number
        room.setType(RoomType.DOUBLE);
        room.setCapacity(2);
        room.setPricePerNight(150000.0);

        // WHEN + THEN
        RoomException thrown = assertThrows(
            RoomException.class,
            () -> callRegister(room)
        );
        assertTrue(thrown.getMessage().contains("Room number is required"), "Should mention missing number");
    }

    // =====================================================================
    // ARCHETYPE 3: NEGATIVE/ZERO VALIDATION
    // Maps to: Scenario 3 in SPEC
    // =====================================================================
    @Test
    @DisplayName("Scenario 3: Should throw exception when price per night is zero — BR-002")
    void shouldThrowExceptionWhenPriceIsZero() throws Exception {
        // GIVEN
        Room room = new Room();
        room.setNumber("102");
        room.setType(RoomType.SINGLE);
        room.setCapacity(1);
        room.setPricePerNight(0.0); // Invalid: zero price

        // WHEN + THEN
        RoomException thrown = assertThrows(
            RoomException.class,
            () -> callRegister(room)
        );
        assertTrue(thrown.getMessage().contains("Price per night must be greater than zero"), "Should mention price rule");
    }

    // =====================================================================
    // ARCHETYPE 4: DUPLICATE DETECTION
    // Maps to: Scenario 4 in SPEC
    // =====================================================================
    @Test
    @DisplayName("Scenario 4: Should throw exception when room number already exists — BR-003")
    void shouldThrowExceptionWhenRoomNumberIsDuplicate() throws Exception {
        // GIVEN
        Room room = new Room();
        room.setNumber("101");
        room.setType(RoomType.DOUBLE);
        room.setCapacity(2);
        room.setPricePerNight(150000.0);

        try {
            // Simulate duplicate exists
            doReturn(true).when(roomDao).existsByNumber(any(), eq("101"));
        } catch (SQLException e) {
            fail("Mock setup should not throw");
        }

        // WHEN + THEN
        RoomException thrown = assertThrows(
            RoomException.class,
            () -> callRegister(room)
        );
        assertTrue(thrown.getMessage().contains("A room with that number already exists"), "Should mention uniqueness rule");
    }

    // =====================================================================
    // ARCHETYPE 5: PRECONDITION NOT MET
    // Maps to: Scenario 5 in SPEC
    // =====================================================================
    @Test
    @DisplayName("Scenario 5: Should throw exception when capacity is invalid — BR-004")
    void shouldThrowExceptionWhenCapacityIsZero() throws Exception {
        // GIVEN
        Room room = new Room();
        room.setNumber("103");
        room.setType(RoomType.SINGLE);
        room.setCapacity(0); // Invalid: zero capacity
        room.setPricePerNight(100000.0);

        // WHEN + THEN
        RoomException thrown = assertThrows(
            RoomException.class,
            () -> callRegister(room)
        );
        assertTrue(thrown.getMessage().contains("Capacity must be greater than zero"), "Should mention capacity rule");
    }

    // =====================================================================
    // ARCHETYPE 6: FORMAT VALIDATION
    // Maps to: Scenario 6 in SPEC
    // =====================================================================
    @Test
    @DisplayName("Scenario 6: Should throw exception when room type is invalid — BR-005")
    void shouldThrowExceptionWhenRoomTypeIsNull() throws Exception {
        // GIVEN
        Room room = new Room();
        room.setNumber("104");
        room.setType(null); // Invalid: null enum
        room.setCapacity(2);
        room.setPricePerNight(150000.0);

        // WHEN + THEN
        RoomException thrown = assertThrows(
            RoomException.class,
            () -> callRegister(room)
        );
        assertTrue(thrown.getMessage().contains("Room type is invalid"), "Should mention invalid type");
    }

    // =====================================================================
    // ARCHETYPE 7: STATE CONFLICT
    // Maps to: Scenario 7 in SPEC
    // =====================================================================
    @Test
    @DisplayName("Scenario 7: Should throw exception when deactivating an occupied room — BR-006")
    void shouldThrowExceptionWhenDeactivatingOccupiedRoom() throws Exception {
        // GIVEN
        Room room = new Room();
        room.setId(1L);
        room.setNumber("105");
        room.setStatus(RoomStatus.OCCUPIED);
        room.setActive(true);

        // WHEN + THEN
        RoomException thrown = assertThrows(
            RoomException.class,
            () -> callDeactivate(room)
        );
        assertTrue(thrown.getMessage().contains("Cannot deactivate a room that is occupied"), "Should mention state conflict");
    }

    // =====================================================================
    // ARCHETYPE 1: HAPPY PATH (UPDATE)
    // Maps to: Scenario 8 in SPEC
    // =====================================================================
    @Test
    @DisplayName("Scenario 8: Should update room successfully — BR-001, BR-002")
    void shouldUpdateRoomSuccessfully() throws Exception {
        // GIVEN
        Room room = new Room();
        room.setId(1L);
        room.setNumber("101");
        room.setType(RoomType.DOUBLE);
        room.setCapacity(2);
        room.setPricePerNight(200000.0);

        try {
            doReturn(false).when(roomDao).existsByNumberAndNotId(any(), eq("101"), eq(1L)); // Unique check for update
            doNothing().when(roomDao).update(any(), any());
        } catch (SQLException e) {
            fail("Mock setup should not throw");
        }

        // WHEN
        Room result = roomService.update(room);

        // THEN
        assertNotNull(result, "Updated room should not be null");
        assertEquals(200000.0, result.getPricePerNight(), "Price should be updated");
    }

    // =====================================================================
    // ARCHETYPE 9: BOUNDARY VALUE
    // Maps to: Scenario 9 in SPEC
    // =====================================================================
    @Test
    @DisplayName("Scenario 9: Should register room with minimum valid price — BR-002")
    void shouldRegisterRoomWithMinimumPrice() throws Exception {
        // GIVEN
        Room room = new Room();
        room.setNumber("106");
        room.setType(RoomType.SINGLE);
        room.setCapacity(1);
        room.setPricePerNight(1.0); // Minimum valid boundary

        try {
            doReturn(false).when(roomDao).existsByNumber(any(), eq("106"));
            doNothing().when(roomDao).insert(any(), any());
        } catch (SQLException e) {
            fail("Mock setup should not throw");
        }

        // WHEN
        Room result = roomService.register(room);

        // THEN
        assertNotNull(result, "Should successfully register room at minimum price boundary");
        assertEquals(1.0, result.getPricePerNight(), "Price should be strictly saved");
    }

    // =====================================================================
    // ARCHETYPE 1 (LIST): HAPPY PATH
    // Maps to: Scenario 10 in SPEC
    // =====================================================================
    @Test
    @DisplayName("Scenario 10: Should filter rooms by type and status — BR-005")
    void shouldFilterRoomsByTypeAndStatus() throws Exception {
        // GIVEN
        Room room = new Room();
        room.setType(RoomType.SUITE);
        room.setStatus(RoomStatus.AVAILABLE);

        try {
            doReturn(Collections.singletonList(room)).when(roomDao).listByTypeAndStatus(any(), eq(RoomType.SUITE), eq(RoomStatus.AVAILABLE));
        } catch (SQLException e) {
            fail("Mock setup should not throw");
        }

        // WHEN
        List<Room> results = roomService.listByTypeAndStatus(RoomType.SUITE, RoomStatus.AVAILABLE);

        // THEN
        assertNotNull(results, "List should not be null");
        assertEquals(1, results.size(), "Should return matching rooms");
        assertEquals(RoomType.SUITE, results.get(0).getType(), "Type should match filter");
        assertEquals(RoomStatus.AVAILABLE, results.get(0).getStatus(), "Status should match filter");
    }
}
