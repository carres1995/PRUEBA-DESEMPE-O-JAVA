# 📋 SPECIFICATION: Reservation Management (Check-in / Check-out)

> **Spec ID:** `SPEC-004`
> **Author:** HotelNova Dev Team
> **Date:** 2026-04-28
> **Status:** [x] Draft | [ ] Approved | [ ] Implemented

---

## 1. Business Goal

**As a** receptionist or administrator of HotelNova,
**I need** to create reservations, record check-ins, and execute check-outs with automatic cost calculation,
**So that** correct room availability, transaction integrity, and financial traceability of each stay are guaranteed.

---

## 2. Hard Business Rules

| Rule ID | Rule Description | Error Behavior |
|---------|-----------------|----------------|
| `BR-001` | The `checkIn` date must be before the `checkOut` date | Throw `ReservationException("Check-in date must be before check-out")` |
| `BR-002` | The room must be in AVAILABLE status to accept a reservation | Throw `ReservationException("Room is not available")` |
| `BR-003` | The guest must be active (`isActive=true`) to make a reservation | Throw `ReservationException("Guest is not active in the system")` |
| `BR-004` | Overlapping reservations for the same room (intersecting dates) are not allowed | Throw `ReservationException("The room already has a reservation in that period")` |
| `BR-005` | Check-out cannot be executed if there is no active reservation (status=CHECKIN) | Throw `ReservationException("There is no active check-in for this reservation")` |
| `BR-006` | The `roomId` and `guestId` fields cannot be null | Throw `ReservationException("Room and guest are required")` |

### 2.1 Calculation Rules

| Calc ID | Description | Formula |
|---------|-------------|---------|
| `CALC-001` | Number of nights of the stay | `DAYS.between(checkIn, checkOut)` |
| `CALC-002` | Subtotal of the stay | `nights × pricePerNight` |
| `CALC-003` | Total cost with VAT | `subtotal × (1 + vat)` where `vat` is read from `config.properties` |

---

## 3. Acceptance Criteria (BDD)

### Scenario 1 — Happy Path: Successful reservation creation (Check-in)
> **Validates:** `BR-001`, `BR-002`, `BR-003`, `BR-004`, `BR-006`

```gherkin
Given a Room with status=AVAILABLE and id=10
  And a Guest with isActive=true and id=5
  And dates checkIn=2026-05-01, checkOut=2026-05-05 (no overlapping)
When  the service ReservationService.createReservation() is invoked within a transaction
Then  the reservation is created with status=CHECKIN
  And the room is set to status=OCCUPIED
  And both changes are confirmed with commit()
```

---

### Scenario 2 — Null/Empty: Null room or guest
> **Validates:** `BR-006`

```gherkin
Given a Reservation where roomId is null
When  the service ReservationService.createReservation() is invoked
Then  a ReservationException is thrown with message "Room and guest are required"
```

---

### Scenario 3 — Invalid Dates: checkIn after checkOut
> **Validates:** `BR-001`

```gherkin
Given a Reservation with checkIn=2026-05-10 and checkOut=2026-05-05
When  the service ReservationService.createReservation() is invoked
Then  a ReservationException is thrown with message "Check-in date must be before check-out"
```

---

### Scenario 4 — Room not available
> **Validates:** `BR-002`

```gherkin
Given a Room with status=OCCUPIED
When  the service ReservationService.createReservation() is invoked for that room
Then  a ReservationException is thrown with message "Room is not available"
```

---

### Scenario 5 — Inactive Guest
> **Validates:** `BR-003`

```gherkin
Given a Guest with isActive=false
When  the service ReservationService.createReservation() is invoked with that guest
Then  a ReservationException is thrown with message "Guest is not active in the system"
  And no changes are persisted in the database
```

---

### Scenario 6 — Overlapping Reservations
> **Validates:** `BR-004`

```gherkin
Given a Room id=10 with an existing reservation from 2026-05-01 to 2026-05-07
  And a new Reservation for the same room from 2026-05-05 to 2026-05-10
When  the service ReservationService.createReservation() is invoked
Then  a ReservationException is thrown with message "The room already has a reservation in that period"
```

---

### Scenario 7 — Check-out without active check-in
> **Validates:** `BR-005`

```gherkin
Given a Reservation with status=FINISHED (not CHECKIN)
When  the service ReservationService.executeCheckout() is invoked
Then  a ReservationException is thrown with message "There is no active check-in for this reservation"
```

---

### Scenario 8 — Stay cost calculation with VAT
> **Validates:** `CALC-001`, `CALC-002`, `CALC-003`

```gherkin
Given a Room with pricePerNight=200000
  And a Reservation with checkIn=2026-05-01 and checkOut=2026-05-04 (3 nights)
  And vat=0.19 read from config.properties
When  the service ReservationService.executeCheckout() is invoked within a transaction
Then  totalCost = 200000 × 3 × 1.19 = 714000.0
  And the reservation remains with status=FINISHED and totalCost=714000.0
  And the room remains with status=AVAILABLE
  And both changes are confirmed with commit()
```

---

### Scenario 9 — Boundary Value: Reservation of exactly 1 night
> **Validates:** `BR-001`, `CALC-001`

```gherkin
Given a Reservation with checkIn=2026-05-01 and checkOut=2026-05-02 (1 night)
When  the service ReservationService.createReservation() is invoked
Then  the operation completes successfully with nights=1
```

---

### Scenario 10 — Rollback on check-in transaction failure
> **Validates:** `BR-002`, JDBC transactions

```gherkin
Given an AVAILABLE Room and an active Guest
  And the room status update fails due to a DB error
When  ReservationService.createReservation() executes the transaction
Then  rollback() is invoked
  And the reservation is NOT persisted
  And the room status remains AVAILABLE
```

---

## 4. UI Requirements (JOptionPane)

| Element Type | Name/Label | Bound to Field | Action/Validation |
|--------------|------------|----------------|-------------------|
| Combo Box | Room | `roomId` | AVAILABLE only (BR-002) |
| Combo Box | Guest | `guestId` | ACTIVE only (BR-003) |
| Date Picker (text) | Check-in Date | `checkIn` | YYYY-MM-DD format, < checkOut (BR-001) |
| Date Picker (text) | Check-out Date | `checkOut` | YYYY-MM-DD format, > checkIn (BR-001) |
| Button | Create Reservation / Check-in | N/A | Calls `ReservationController.createReservation()` |
| Button | Execute Check-out | N/A | Calls `ReservationController.executeCheckout()` |
| Label | Total Cost (preview) | `totalCost` | Calculated upon confirming checkout |
| Data Table | Active Reservations | `List<Reservation>` | ID, Room, Guest, CheckIn, CheckOut, Status |

---

## 5. Technical Notes

---

### 5.1 Artifacts to Generate

| Layer | Class Name | Key Responsibility |
|-------|-----------|-------------------|
| Exception | `ReservationException.java` | Business rule violations in reservations |
| Model | `Reservation.java` | Fields: `Long id`, `Long roomId`, `Long guestId`, `LocalDate checkIn`, `LocalDate checkOut`, `ReservationStatus status`, `double totalCost`, `LocalDateTime createdAt` |
| Enum | `ReservationStatus.java` | ACTIVE, CHECKIN, FINISHED, CANCELLED |
| DAO Interface | `ReservationDao.java` | `insert`, `update`, `findById`, `listActive`, `listByGuest`, `checkOverlap`, `findByRoomAndStatus` |
| DAO Impl | `ReservationDaoImpl.java` | JDBC implementation with transactions |
| Service | `ReservationService.java` | BR-001..BR-006 validations, CALC-001..003, transaction orchestration |
| Util | `ConfigUtil.java` | Reads `config.properties` (vat, checkInHour, checkOutHour) |
| Controller | `ReservationController.java` | Interaction with JOptionPane |
| Test | `ReservationServiceTest.java` | Scenarios 1–10 |

### 5.2 SQL Queries

```sql
-- Verify availability (BR-002)
SELECT status FROM rooms WHERE id = ?;

-- Verify active guest (BR-003)
SELECT is_active FROM guests WHERE id = ?;

-- Detect overlap (BR-004)
SELECT COUNT(*) FROM reservations
WHERE room_id = ?
  AND status NOT IN ('FINISHED','CANCELLED')
  AND check_in < ? AND check_out > ?;

-- Insert reservation
INSERT INTO reservations (room_id, guest_id, check_in, check_out, status, total_cost, created_at)
VALUES (?, ?, ?, ?, 'CHECKIN', 0, NOW());

-- Update room status to OCCUPIED (part of check-in transaction)
UPDATE rooms SET status = 'OCCUPIED' WHERE id = ?;

-- Find active reservation for checkout (BR-005)
SELECT * FROM reservations WHERE id = ? AND status = 'CHECKIN';

-- Update reservation at checkout
UPDATE reservations SET status = 'FINISHED', total_cost = ? WHERE id = ?;

-- Update room status to AVAILABLE (part of check-out transaction)
UPDATE rooms SET status = 'AVAILABLE' WHERE id = ?;

-- List active reservations for CSV export
SELECT r.id, h.number, g.first_name, g.last_name, r.check_in, r.check_out, r.status, r.total_cost
FROM reservations r
JOIN rooms h ON r.room_id = h.id
JOIN guests g ON r.guest_id = g.id
WHERE r.status IN ('ACTIVE','CHECKIN');
```

### 5.3 DDL

```sql
CREATE TABLE reservations (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  room_id        BIGINT NOT NULL,
  guest_id       BIGINT NOT NULL,
  check_in       DATE NOT NULL,
  check_out      DATE NOT NULL,
  status         ENUM('ACTIVE','CHECKIN','FINISHED','CANCELLED') NOT NULL DEFAULT 'ACTIVE',
  total_cost    DECIMAL(14,2) DEFAULT 0,
  created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_room  FOREIGN KEY (room_id)  REFERENCES rooms(id),
  CONSTRAINT fk_guest FOREIGN KEY (guest_id) REFERENCES guests(id)
);
```

### 5.4 Check-in Transaction

```java
connection.setAutoCommit(false);
try {
    reservationDao.insert(reservation, connection);
    roomDao.updateStatus(roomId, "OCCUPIED", connection);
    connection.commit();
} catch (Exception e) {
    connection.rollback();
    throw new ReservationException("Error in check-in: " + e.getMessage());
}
```

### 5.5 Check-out Transaction

```java
connection.setAutoCommit(false);
try {
    double totalCost = calculateCost(reservation);  // CALC-001, CALC-002, CALC-003
    reservationDao.finish(reservationId, totalCost, connection);
    roomDao.updateStatus(roomId, "AVAILABLE", connection);
    connection.commit();
} catch (Exception e) {
    connection.rollback();
    throw new ReservationException("Error in check-out: " + e.getMessage());
}
```

### 5.6 config.properties (relevant fragment)

```properties
vat=0.19
checkInHour=15
checkOutHour=12
```

### 5.7 CSV Export

- `active_reservations.csv` — columns: `id,room,guest,check_in,check_out,status,total_cost`

### 5.8 Dependencies

- [ ] `SPEC-001` — `rooms` table must exist
- [ ] `SPEC-003` — `guests` table must exist
- [ ] `ConfigUtil` must be able to read `vat` from `config.properties`

---

## 6. Out of Scope

- Reservation cancellation with refund policy — `SPEC-005`
- Group reservations (multiple rooms) — `SPEC-006`
- Integration with payment gateway — frontend team

---

## 7. Approval

| Role | Name | Date | ✓ |
|------|------|------|---|
| Tech Lead | | | [ ] |
| Product Owner | | | [ ] |

---

---

> **⚠️ Do not generate code until this spec is approved.**
