# 📋 SPECIFICATION: Room Management

> **Spec ID:** `SPEC-001`
> **Author:** HotelNova Dev Team
> **Date:** 2026-04-28
> **Status:** [x] Draft | [ ] Approved | [ ] Implemented

---

## 1. Business Goal

**As a** administrator or receptionist of the HotelNova system,
**I need** to register, edit, activate/deactivate, and list rooms with uniqueness and availability validations,
**So that** the integrity of the room inventory is guaranteed and duplications or inconsistent states are avoided.

---

## 2. Hard Business Rules

| Rule ID | Rule Description | Error Behavior |
|---------|-----------------|----------------|
| `BR-001` | Room `number` cannot be null or empty | Throw `RoomException("Room number is required")` |
| `BR-002` | `pricePerNight` must be greater than zero | Throw `RoomException("Price per night must be greater than zero")` |
| `BR-003` | Room `number` must be unique in the database | Throw `RoomException("A room with that number already exists")` |
| `BR-004` | `capacity` must be an integer greater than zero | Throw `RoomException("Capacity must be greater than zero")` |
| `BR-005` | `type` must be one of: SINGLE, DOUBLE, SUITE | Throw `RoomException("Room type is invalid")` |
| `BR-006` | A room with status OCCUPIED cannot be deactivated | Throw `RoomException("Cannot deactivate a room that is occupied")` |

### 2.1 Calculation Rules

> _(Not applicable for this module)_

---

## 3. Acceptance Criteria (BDD)

### Scenario 1 — Happy Path: Successful room registration
> **Validates:** `BR-001`, `BR-002`, `BR-003`, `BR-004`, `BR-005`

```gherkin
Given a Room with number="101", type=DOUBLE, capacity=2, pricePerNight=150000, status=AVAILABLE, isActive=true
When  the service RoomService.register() is invoked
Then  the operation completes successfully
  And the room is persisted with createdAt assigned automatically
```

---

### Scenario 2 — Null/Empty: Empty room number
> **Validates:** `BR-001`

```gherkin
Given a Room where number is null
When  the service RoomService.register() is invoked
Then  a RoomException is thrown with message "Room number is required"
```

---

### Scenario 3 — Negative/Zero: Price per night equal to zero
> **Validates:** `BR-002`

```gherkin
Given a Room where pricePerNight = 0
When  the service RoomService.register() is invoked
Then  a RoomException is thrown with message "Price per night must be greater than zero"
```

---

### Scenario 4 — Duplicate Detection: Duplicate room number
> **Validates:** `BR-003`

```gherkin
Given a Room with number="101" that already exists in the database
When  the service RoomService.register() is invoked
Then  a RoomException is thrown with message "A room with that number already exists"
```

---

### Scenario 5 — Precondition Not Met: Invalid capacity
> **Validates:** `BR-004`

```gherkin
Given a Room where capacity = 0
When  the service RoomService.register() is invoked
Then  a RoomException is thrown with message "Capacity must be greater than zero"
  And no record is persisted in the database
```

---

### Scenario 6 — Format Validation: Invalid room type
> **Validates:** `BR-005`

```gherkin
Given a Room where type = "PRESIDENTIAL" (forbidden value)
When  the service RoomService.register() is invoked
Then  a RoomException is thrown with message "Room type is invalid"
```

---

### Scenario 7 — State Conflict: Deactivate occupied room
> **Validates:** `BR-006`

```gherkin
Given a Room with status=OCCUPIED and isActive=true
When  the service RoomService.deactivate() is invoked
Then  a RoomException is thrown with message "Cannot deactivate a room that is occupied"
```

---

### Scenario 8 — Happy Path: Successful room update
> **Validates:** `BR-001`, `BR-002`

```gherkin
Given an existing Room with id=1 and pricePerNight=100000
When  the service RoomService.update() is invoked with pricePerNight=200000
Then  the room is updated with pricePerNight=200000
```

---

### Scenario 9 — Boundary Value: Minimum valid price (1 unit)
> **Validates:** `BR-002`

```gherkin
Given a Room where pricePerNight = 1
When  the service RoomService.register() is invoked
Then  the operation completes successfully with pricePerNight=1
```

---

### Scenario 10 — Filtering by type and status
> **Validates:** `BR-005`

```gherkin
Given registered rooms with different types and statuses
When  the service RoomService.listByTypeAndStatus(type=SUITE, status=AVAILABLE) is invoked
Then  it returns only rooms of type SUITE with status AVAILABLE
```

---

## 4. UI Requirements (JOptionPane)

| Element Type | Name/Label | Bound to Field | Action/Validation |
|--------------|------------|----------------|-------------------|
| Input Text | Number | `number` | Required (BR-001), unique (BR-003) |
| Combo Box | Type | `type` | Options: SINGLE, DOUBLE, SUITE (BR-005) |
| Input Number | Capacity | `capacity` | Integer > 0 (BR-004) |
| Input Number | Price per Night | `pricePerNight` | Decimal > 0 (BR-002) |
| Combo Box | Status | `status` | AVAILABLE / OCCUPIED |
| Check Box | Active | `isActive` | Boolean |
| Button | Save | N/A | Calls `RoomController.save()` |
| Button | Edit | N/A | Calls `RoomController.edit()` |
| Button | Activate/Deactivate | N/A | Calls `RoomController.toggleActive()` |
| Data Table | Room List | `List<Room>` | Columns aligned with labels [ACTIVE]/[INACTIVE], [AVAILABLE]/[OCCUPIED] |
| Combo Box (filter) | Filter by Type | `type` | Filters table in real time |
| Combo Box (filter) | Filter by Status | `status` | Filters table in real time |

---

## 5. Technical Notes

---

### 5.1 Artifacts to Generate

| Layer | Class Name | Key Responsibility |
|-------|-----------|-------------------|
| Exception | `RoomException.java` | Business rule violations for rooms |
| Model | `Room.java` | Fields: `Long id`, `String number`, `RoomType type`, `int capacity`, `double pricePerNight`, `RoomStatus status`, `boolean isActive`, `LocalDateTime createdAt` |
| Enum | `RoomType.java` | SINGLE, DOUBLE, SUITE |
| Enum | `RoomStatus.java` | AVAILABLE, OCCUPIED |
| DAO Interface | `RoomDao.java` | `register`, `update`, `findById`, `findByNumber`, `listAll`, `listByType`, `listByStatus`, `delete` |
| DAO Impl | `RoomDaoImpl.java` | JDBC implementation |
| Service | `RoomService.java` | BR-001..BR-006 validations + business logic |
| Controller | `RoomController.java` | Interaction with JOptionPane |
| Test | `RoomServiceTest.java` | Scenarios 1–10 |

### 5.2 SQL Queries

```sql
-- Find by ID
SELECT id, number, type, capacity, price_per_night, status, is_active, created_at
FROM rooms WHERE id = ?;

-- Find by number (for uniqueness validation BR-003)
SELECT COUNT(*) FROM rooms WHERE number = ?;

-- Insert
INSERT INTO rooms (number, type, capacity, price_per_night, status, is_active, created_at)
VALUES (?, ?, ?, ?, ?, ?, NOW());

-- Update
UPDATE rooms
SET number=?, type=?, capacity=?, price_per_night=?, status=?, is_active=?
WHERE id=?;

-- Delete
DELETE FROM rooms WHERE id=?;

-- Filter by type and status
SELECT * FROM rooms WHERE type=? AND status=?;
```

### 5.3 DDL

```sql
CREATE TABLE rooms (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  number           VARCHAR(10) NOT NULL UNIQUE,
  type             ENUM('SINGLE','DOUBLE','SUITE') NOT NULL,
  capacity         INT NOT NULL,
  price_per_night DECIMAL(12,2) NOT NULL,
  status           ENUM('AVAILABLE','OCCUPIED') NOT NULL DEFAULT 'AVAILABLE',
  is_active        BOOLEAN NOT NULL DEFAULT TRUE,
  created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### 5.4 Dependencies

- [ ] DB table `rooms` must exist
- [ ] `config.properties` with available DB credentials

---

## 6. Out of Scope

- Management of dynamic seasonal prices — `SPEC-005`
- Room photographs — frontend team
- Integration with external booking systems

---

## 7. Approval

| Role | Name | Date | ✓ |
|------|------|------|---|
| Tech Lead | | | [ ] |
| Product Owner | | | [ ] |

---

> **⚠️ Do not generate code until this spec is approved.**