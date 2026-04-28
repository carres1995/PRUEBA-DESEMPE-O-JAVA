# 📋 SPECIFICATION: Guest Management

> **Spec ID:** `SPEC-003`
> **Author:** HotelNova Dev Team
> **Date:** 2026-04-28
> **Status:** [x] Draft | [ ] Approved | [ ] Implemented

---

## 1. Business Goal

**As a** receptionist or administrator of HotelNova,
**I need** to register, edit, and manage the status of hotel guests,
**So that** reservations are linked to real and identified persons, avoiding inconsistent records or duplicates by identity document.

---

## 2. Hard Business Rules

| Rule ID | Rule Description | Error Behavior |
|---------|-----------------|----------------|
| `BR-001` | `firstName` cannot be null or empty | Throw `GuestException("Guest first name is required")` |
| `BR-002` | `lastName` cannot be null or empty | Throw `GuestException("Guest last name is required")` |
| `BR-003` | `documentNumber` (identity number) must be unique | Throw `GuestException("A guest with that document number already exists")` |
| `BR-004` | `email` must have a valid format if provided (basic regex) | Throw `GuestException("The email format is invalid")` |
| `BR-005` | `phone` must contain only digits if provided (7-15 digits) | Throw `GuestException("The phone must contain between 7 and 15 digits")` |
| `BR-006` | A guest with active reservations cannot be deactivated | Throw `GuestException("Cannot deactivate a guest with active reservations")` |

---

## 3. Acceptance Criteria (BDD)

### Scenario 1 — Happy Path: Successful guest registration
> **Validates:** `BR-001`, `BR-002`, `BR-003`, `BR-004`, `BR-005`

```gherkin
Given a Guest with firstName="Carlos", lastName="Ruiz", documentNumber="12345678",
      email="carlos@mail.com", phone="3001234567", isActive=true
When  the service GuestService.register() is invoked
Then  the operation completes successfully
  And the createdAt field is automatically assigned
```

---

### Scenario 2 — Null/Empty: Empty first name
> **Validates:** `BR-001`

```gherkin
Given a Guest where firstName is null
When  the service GuestService.register() is invoked
Then  a GuestException is thrown with message "Guest first name is required"
```

---

### Scenario 3 — Null/Empty: Empty last name
> **Validates:** `BR-002`

```gherkin
Given a Guest where lastName is null or empty
When  the service GuestService.register() is invoked
Then  a GuestException is thrown with message "Guest last name is required"
```

---

### Scenario 4 — Duplicate Detection: Duplicate document number
> **Validates:** `BR-003`

```gherkin
Given a Guest with documentNumber="12345678" that already exists in the database
When  the service GuestService.register() is invoked
Then  a GuestException is thrown with message "A guest with that document number already exists"
```

---

### Scenario 5 — Format Validation: Invalid email
> **Validates:** `BR-004`

```gherkin
Given a Guest where email = "mail-without-at"
When  the service GuestService.register() is invoked
Then  a GuestException is thrown with message "The email format is invalid"
```

---

### Scenario 6 — Format Validation: Phone with letters
> **Validates:** `BR-005`

```gherkin
Given a Guest where phone = "ABC123XYZ"
When  the service GuestService.register() is invoked
Then  a GuestException is thrown with message "The phone must contain between 7 and 15 digits"
```

---

### Scenario 7 — State Conflict: Deactivate guest with active reservations
> **Validates:** `BR-006`

```gherkin
Given a Guest with isActive=true who has reservations in ACTIVE status
When  the service GuestService.deactivate() is invoked
Then  a GuestException is thrown with message "Cannot deactivate a guest with active reservations"
```

---

### Scenario 8 — Happy Path: Successful guest editing
> **Validates:** `BR-001`, `BR-004`

```gherkin
Given an existing Guest with id=5
When  the service GuestService.update() is invoked with email="new@mail.com"
Then  the operation completes successfully
  And the guest's email is updated
```

---

### Scenario 9 — Boundary Value: Phone with 7 digits (minimum valid)
> **Validates:** `BR-005`

```gherkin
Given a Guest where phone = "1234567" (7 digits)
When  the service GuestService.register() is invoked
Then  the operation completes successfully
```

---

### Scenario 10 — Allowed deactivation: Guest without active reservations
> **Validates:** `BR-006`

```gherkin
Given a Guest with isActive=true who DOES NOT have active reservations
When  the service GuestService.deactivate() is invoked
Then  the guest remains with isActive=false
  And the change is persisted in the database
```

---

## 4. UI Requirements (JOptionPane)

| Element Type | Name/Label | Bound to Field | Action/Validation |
|--------------|------------|----------------|-------------------|
| Input Text | First Name | `firstName` | Required (BR-001) |
| Input Text | Last Name | `lastName` | Required (BR-002) |
| Input Text | Document | `documentNumber` | Required, unique (BR-003) |
| Input Text | Email | `email` | Email format (BR-004) |
| Input Text | Phone | `phone` | Digits only, 7-15 (BR-005) |
| Check Box | Active | `isActive` | Boolean |
| Button | Save | N/A | Calls `GuestController.save()` |
| Button | Edit | N/A | Calls `GuestController.edit()` |
| Button | Deactivate | N/A | Calls `GuestController.toggleActive()` |
| Data Table | Guest List | `List<Guest>` | Columns: ID, First Name, Last Name, Document, Email, [ACTIVE]/[INACTIVE] |

---

## 5. Technical Notes

---

### 5.1 Artifacts to Generate

| Layer | Class Name | Key Responsibility |
|-------|-----------|-------------------|
| Exception | `GuestException.java` | Business rule violations for guests |
| Model | `Guest.java` | Fields: `Long id`, `String firstName`, `String lastName`, `String documentNumber`, `String email`, `String phone`, `boolean isActive`, `LocalDateTime createdAt` |
| DAO Interface | `GuestDao.java` | `register`, `update`, `findById`, `findByDocument`, `listAll`, `listActive`, `delete` |
| DAO Impl | `GuestDaoImpl.java` | JDBC implementation |
| Service | `GuestService.java` | BR-001..BR-006 validations |
| Controller | `GuestController.java` | Interaction with JOptionPane |
| Test | `GuestServiceTest.java` | Scenarios 1–10 |

### 5.2 SQL Queries

```sql
-- Find by document (uniqueness BR-003)
SELECT COUNT(*) FROM guests WHERE document_number = ?;

-- Find by ID
SELECT id, first_name, last_name, document_number, email, phone, is_active, created_at
FROM guests WHERE id = ?;

-- List active
SELECT * FROM guests WHERE is_active = TRUE;

-- Verify guest's active reservations (BR-006)
SELECT COUNT(*) FROM reservations
WHERE guest_id = ? AND status IN ('ACTIVE', 'CHECKIN');

-- Insert
INSERT INTO guests (first_name, last_name, document_number, email, phone, is_active, created_at)
VALUES (?, ?, ?, ?, ?, ?, NOW());

-- Update
UPDATE guests
SET first_name=?, last_name=?, document_number=?, email=?, phone=?, is_active=?
WHERE id=?;

-- Delete
DELETE FROM guests WHERE id=?;
```

### 5.3 DDL

```sql
CREATE TABLE guests (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  first_name      VARCHAR(100) NOT NULL,
  last_name       VARCHAR(100) NOT NULL,
  document_number VARCHAR(20) NOT NULL UNIQUE,
  email           VARCHAR(150),
  phone           VARCHAR(15),
  is_active       BOOLEAN NOT NULL DEFAULT TRUE,
  created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### 5.4 Dependencies

- [ ] DB table `guests` must exist
- [ ] `SPEC-004` (Reservations) depends on this entity to validate `BR-006`

---

## 6. Out of Scope

- Stay history per guest — `SPEC-005`
- Loyalty program / points
- Bulk guest upload via CSV

---

## 7. Approval

| Role | Name | Date | ✓ |
|------|------|------|---|
| Tech Lead | | | [ ] |
| Product Owner | | | [ ] |

---

> **⚠️ Do not generate code until this spec is approved.**