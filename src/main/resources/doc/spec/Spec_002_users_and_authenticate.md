# 📋 SPECIFICATION: User Management and Authentication

> **Spec ID:** `SPEC-002`
> **Author:** HotelNova Dev Team
> **Date:** 2026-04-28
> **Status:** [x] Draft | [ ] Approved | [ ] Implemented

---

## 1. Business Goal

**As a** administrator of the HotelNova system,
**I need** to register users with differentiated roles and authenticate their access using secure credentials,
**So that** only authorized personnel can operate the system according to their role (ADMIN or RECEPTIONIST), ensuring traceability and security.

---

## 2. Hard Business Rules

| Rule ID | Rule Description | Error Behavior |
|---------|-----------------|----------------|
| `BR-001` | `username` cannot be null or empty | Throw `UserException("Username is required")` |
| `BR-002` | `password` cannot be null and must have at least 8 characters | Throw `UserException("Password must be at least 8 characters long")` |
| `BR-003` | `username` must be unique in the database | Throw `UserException("Username is already registered")` |
| `BR-004` | `role` must be one of: ADMIN, RECEPTIONIST | Throw `UserException("The specified role is invalid")` |
| `BR-005` | Passwords must be stored as BCrypt hashes (never in plain text) | Throw `UserException("Error processing the password")` |
| `BR-006` | An inactive user cannot log in | Throw `AuthenticationException("Inactive user. Contact the administrator")` |
| `BR-007` | Incorrect credentials must return a generic error (without revealing if the user exists) | Throw `AuthenticationException("Invalid credentials")` |

---

## 3. Acceptance Criteria (BDD)

### Scenario 1 — Happy Path: Successful registration of an ADMIN user
> **Validates:** `BR-001`, `BR-002`, `BR-003`, `BR-004`, `BR-005`

```gherkin
Given a User with username="admin01", password="Admin2024!", role=ADMIN, isActive=true
When  the service UserService.register() is invoked
Then  the operation completes successfully
  And the password stored in DB is a BCrypt hash (not plain text)
  And the createdAt field is automatically assigned
```

---

### Scenario 2 — Null/Empty: Empty username
> **Validates:** `BR-001`

```gherkin
Given a User where username is null
When  the service UserService.register() is invoked
Then  a UserException is thrown with message "Username is required"
```

---

### Scenario 3 — Negative/Zero: Password with fewer than 8 characters
> **Validates:** `BR-002`

```gherkin
Given a User where password = "abc123"  (6 characters)
When  the service UserService.register() is invoked
Then  a UserException is thrown with message "Password must be at least 8 characters long"
```

---

### Scenario 4 — Duplicate Detection: Duplicate username
> **Validates:** `BR-003`

```gherkin
Given a User with username="admin01" that already exists in the database
When  the service UserService.register() is invoked
Then  a UserException is thrown with message "Username is already registered"
```

---

### Scenario 5 — Precondition Not Met: Invalid role
> **Validates:** `BR-004`

```gherkin
Given a User with role = "SUPERVISOR" (forbidden value)
When  the service UserService.register() is invoked
Then  a UserException is thrown with message "The specified role is invalid"
  And no record is persisted in the database
```

---

### Scenario 6 — Happy Path: Successful login with role RECEPTIONIST
> **Validates:** `BR-006`, `BR-007`

```gherkin
Given an active User with username="recep01" and correct password
When  the service AuthenticationService.login("recep01", "password") is invoked
Then  authentication is successful
  And it returns the User object with role=RECEPTIONIST
```

---

### Scenario 7 — State Conflict: Login with inactive user
> **Validates:** `BR-006`

```gherkin
Given a User with isActive=false
When  the service AuthenticationService.login() is invoked with correct credentials
Then  an AuthenticationException is thrown with message "Inactive user. Contact the administrator"
```

---

### Scenario 8 — Login with incorrect credentials
> **Validates:** `BR-007`

```gherkin
Given a valid username but with incorrect password
When  the service AuthenticationService.login() is invoked
Then  an AuthenticationException is thrown with message "Invalid credentials"
  And the message DOES NOT reveal whether the user exists or not
```

---

### Scenario 9 — Boundary Value: Password exactly 8 characters long
> **Validates:** `BR-002`

```gherkin
Given a User where password = "Ab123456" (exactly 8 characters)
When  the service UserService.register() is invoked
Then  the operation completes successfully
```

---

### Scenario 10 — Hash verification: Password never in plain text
> **Validates:** `BR-005`

```gherkin
Given a registered User with password="Secure2024!"
When  the DB record is directly queried
Then  the password_hash field DOES NOT contain "Secure2024!"
  And the hash is verifiable with BCrypt.checkpw()
```

---

## 4. UI Requirements (JOptionPane)

| Element Type | Name/Label | Bound to Field | Action/Validation |
|--------------|------------|----------------|-------------------|
| Input Text | User | `username` | Required (BR-001), unique (BR-003) |
| Password Field | Password | `password` | Min 8 chars (BR-002) |
| Combo Box | Role | `role` | Options: ADMIN, RECEPTIONIST (BR-004) |
| Check Box | Active | `isActive` | Boolean |
| Button | Log In | N/A | Calls `AuthenticationController.login()` |
| Button | Register User | N/A | Only visible for ADMIN |
| Button | Deactivate | N/A | Calls `UserController.toggleActive()` |
| Data Table | User List | `List<User>` | Shows id, username, role, [ACTIVE]/[INACTIVE] |

---

## 5. Technical Notes

### 5.1 Artifacts to Generate

| Layer | Class Name | Key Responsibility |
|-------|-----------|-------------------|
| Exception | `UserException.java` | Business rule violations in users |
| Exception | `AuthenticationException.java` | Login failures |
| Model | `User.java` | Fields: `Long id`, `String username`, `String passwordHash`, `Role role`, `boolean isActive`, `LocalDateTime createdAt` |
| Enum | `Role.java` | ADMIN, RECEPTIONIST |
| DAO Interface | `UserDao.java` | `register`, `findByUsername`, `findById`, `listAll`, `update`, `delete` |
| DAO Impl | `UserDaoImpl.java` | JDBC implementation |
| Service | `UserService.java` | BR-001..BR-005 validations + BCrypt hash |
| Service | `AuthenticationService.java` | Login with BR-006, BR-007 validation |
| Controller | `UserController.java` | Interaction with JOptionPane |
| Controller | `AuthenticationController.java` | Login workflow |
| Util | `PasswordUtil.java` | Encapsulate BCrypt hash/verify |
| Test | `UserServiceTest.java` | Scenarios 1–10 |

### 5.2 SQL Queries

```sql
-- Find by username (for login and duplicate check)
SELECT id, username, password_hash, role, is_active, created_at
FROM users WHERE username = ?;

-- Verify uniqueness (BR-003)
SELECT COUNT(*) FROM users WHERE username = ?;

-- Insert
INSERT INTO users (username, password_hash, role, is_active, created_at)
VALUES (?, ?, ?, ?, NOW());

-- Update
UPDATE users SET username=?, password_hash=?, role=?, is_active=? WHERE id=?;

-- Delete
DELETE FROM users WHERE id=?;
```

### 5.3 DDL

```sql
CREATE TABLE users (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  username      VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role          ENUM('ADMIN','RECEPTIONIST') NOT NULL,
  is_active     BOOLEAN NOT NULL DEFAULT TRUE,
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### 5.4 Dependency: BCrypt

```xml
<!-- pom.xml -->
<dependency>
  <groupId>org.mindrot</groupId>
  <artifactId>jbcrypt</artifactId>
  <version>0.4</version>
</dependency>
```

### 5.5 Logging (Simulated HTTP)

```
[POST] /api/users/login - username=recep01 - 200 OK
[POST] /api/users/login - username=unknown  - 401 UNAUTHORIZED
[POST] /api/users       - username=admin02  - 201 CREATED
[PATCH] /api/users/3    - isActive=false    - 200 OK
```

### 5.6 Dependencies

- [ ] DB table `users` must exist
- [ ] BCrypt dependency available in `pom.xml`
- [ ] `config.properties` with available DB credentials

---

## 6. Out of Scope

- Multi-factor authentication — `SPEC-006`
- Password recovery via email — frontend team
- Granular permissions per operation

---

## 7. Approval

| Role | Name | Date | ✓ |
|------|------|------|---|
| Tech Lead | | | [ ] |
| Product Owner | | | [ ] |

---

> **⚠️ Do not generate code until this spec is approved.**