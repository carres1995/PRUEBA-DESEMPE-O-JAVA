# 🏨 HotelNova — Hotel Management System

> **Performance Test — Module 5.1 Java SE**  
> Java SE + JavaFX + JDBC + Layered Architecture + Files + Exceptions + Unit Testing

---

## 👨‍💻 Developer Information

| Field | Information |
|-------|-------------|
| **Name** | [Your Full Name] |
| **Clan** | [Your Clan] |
| **Email** | [your.email@riwi.io] |
| **Document ID** | [Your Document Number] |

---

## 📖 System Overview

**HotelNova** is a hotel management system developed in **Java SE 17+** with a **JavaFX** graphical interface, **pure JDBC** persistence, and a **strict Layered MVC** architecture. It solves the following operational issues:

| Original Problem | Implemented Solution |
|------------------|-----------------------|
| Duplicate reservations due to incorrect IDs | Date overlap validation + UNIQUE constraints |
| Availability inconsistencies | JDBC Transactions: Atomic Check-in/out |
| Lack of user and role control | Authentication with BCrypt + RBAC (ADMIN/RECEPTIONIST) |
| Incomplete or erroneous Check-in/out | Transactional flow with business validations |
| Information loss | Logs in DB, `app.log` file, and CSV export |

---

## 🛠️ Technology Stack

| Component | Technology |
|------------|-----------|
| Language | Java 17+ |
| Graphical Interface | JavaFX 21 |
| Build Tool | Apache Maven |
| Database (Production) | PostgreSQL (Supabase) |
| Database (Testing) | H2 In-Memory |
| Data Access | Pure JDBC (`java.sql`) |
| Password Hashing | jBCrypt |
| Unit Testing | JUnit 5 + Mockito |
| Architecture | Layered MVC (Controller → Service → DAO → DB) |

---

## 💡 Solutions to Client Requirements

### 1️⃣ Room Management
-  Room registration with: `id`, `number`, `type`, `capacity`, `price_per_night`, `status`, `is_active`, `created_at`
-  Editing and updating existing rooms
-  Unique room number validation (`BR-003`)
-  Listing and filtering by type or status directly from the database
-  Visual table with aligned columns in the JavaFX UI
-  Dynamic inventory: room numbers loaded from `room_inventory`

### 2️⃣ User Management & Authentication
-  Login with credential and role validation (`ADMIN` / `RECEPTIONIST`)
-  Passwords stored with **BCrypt hash** (never in plain text)
-  Full User CRUD with mandatory `email` field
-  User activation/deactivation (toggle)
-  HTTP Logs in console: `[POST] /auth/login → [200]`
-  "Manage Users" menu restricted to `ADMIN` only

### 3️⃣ Graphical Interface (JavaFX)
-  Single-Stage with Scene Manager (one Stage, multiple Scenes)
-  Central Dashboard with navigation to all modules
-  Module menus: Rooms, Guests, Users, Reservations, Exports
-  Confirmations and success/error messages using `Alert`
-  Tables with full columns and resizing policies
-  Context menus (right-click) for status operations

### 4️⃣ CRUD + JDBC + Transactions
-  DAO interfaces defined in `dao/interfaces/`
-  JDBC implementations using `PreparedStatement` + PostgreSQL casting (`::room_type`)
-  **Check-in**: `setAutoCommit(false)` → insert reservation → update room to `OCCUPIED` → `commit()` / `rollback()`
-  **Check-out**: update reservation to `COMPLETED` → calculate total cost + VAT → update room to `AVAILABLE` → `commit()`
-  `try-with-resources` used in all connections and statements

### 5️⃣ File Handling
-  `config.properties` with: `vat=0.19`, `checkInHour=15`, `checkOutHour=12`
-  `database.properties` with: `db.url`, `db.user`, `db.password`, `db.driver`
-  CSV Export:
  - `exports/rooms_report.csv` — full listing
  - `exports/active_reservations.csv` — active reservations only
-  Activity logging in `app.log` using `java.util.logging`

### 6️⃣ Exceptions & Validations
-  Custom Exceptions: `RoomException`, `GuestException`, `ReservationException`, `UserException`, `AuthenticationException`
-  Business validations implemented:
  - Unique room number
  - Room availability for reservation
  - Active guest for reservation
  - Valid dates: `checkIn < checkOut`
  - No date overlapping for the same room
  - No check-out allowed without a prior check-in
  - **BR-006**: Cannot deactivate a guest with active reservations
-  Errors displayed in JavaFX `Alert`
-  Details stored in `app.log` and `activity_logs` table

### 7️⃣ Unit Testing (JUnit 5)
-  **42 tests** running with `mvn test — BUILD SUCCESS`
-  `RoomServiceTest` (10 tests): unique number, availability, types, prices
-  `GuestServiceTest` (11 tests): unique document, email, phone, BR-006
-  `ReservationServiceTest` (10 tests): dates, overlap, check-in/out, cost with VAT
-  `UserServiceTest` (11 tests): credentials, roles, BCrypt, email

---

## 📋 Prerequisites

| Tool | Minimum Version | Verification |
|-------------|---------------|-------------|
| Java JDK | 17+ | `java -version` |
| Apache Maven | 3.8+ | `mvn -version` |
| PostgreSQL | 14+ (or Supabase) | Remote DB access included |
| Git | Any | `git --version` |

> 💡 **No local PostgreSQL installation is required.** The application connects to a pre-configured **Supabase** instance.

---

## 🚀 Setup and Execution

### 1. Clone the repository

```bash
git clone https://github.com/[your-user]/hotelNova.git
cd hotelNova
```

### 2. Verify Database Configuration

The `src/main/resources/database.properties` file already contains the Supabase connection:

```properties
db.url      = jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres?currentSchema=hotel
db.user     = [user]
db.password = [password]
db.driver   = org.postgresql.Driver
```

> ⚠️ If using your own PostgreSQL DB, update these values with your credentials.

### 3. Verify Business Parameters

File `src/main/resources/config.properties`:

```properties
vat=0.19          # 19% VAT applied to the total stay cost
checkInHour=15    # Official check-in time (3:00 PM)
checkOutHour=12   # Official check-out time (12:00 PM)
```

### 4. Run Unit Tests

```bash
mvn test
```

**Expected Result:**
```
Tests run: 42, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### 5. Run the Application

```bash
mvn javafx:run
```

**Upon startup, the system automatically:**
1. Connects to PostgreSQL
2. Executes initialization SQL scripts (`sql/*.sql`)
3. Displays the Login screen

### 6. Default Credentials

| User | Password | Role |
|---------|-----------|-----|
| `admin` | `admin123` | ADMIN |

---

## 🗂️ Project Structure

```
hotelNova/
├── pom.xml                          # Maven configuration + dependencies
├── README.md                        # This file
├── app.log                          # Activity log (generated at runtime)
├── exports/                         # Exported CSV files (generated at runtime)
│
└── src/
    ├── main/
    │   ├── java/com/hotel/
    │   │   ├── config/              # DB config and initialization
    │   │   │   ├── AppConfig.java
    │   │   │   ├── ConnectionFactory.java
    │   │   │   └── DatabaseInitializer.java
    │   │   ├── controller/          # Input layer (MVC Controller)
    │   │   ├── dao/                 # Data access layer (JDBC)
    │   │   │   ├── interfaces/      # DAO contracts
    │   │   │   ├── queries/         # SQL constants
    │   │   │   ├── AbstractDao.java # Base generic logic
    │   │   │   └── ...              # Specific DAOs
    │   │   ├── exception/           # Custom exceptions
    │   │   ├── model/               # Domain entities
    │   │   ├── service/             # Business logic + transactions
    │   │   ├── util/                # Cross-cutting utilities
    │   │   │   ├── AppLogger.java   # Logger: console + file + DB
    │   │   │   ├── CsvExporter.java # CSV Exporter (with FileChooser)
    │   │   │   └── ...
    │   │   └── view/                # Presentation layer (JavaFX)
    │   └── resources/
    │       ├── config.properties    # Business parameters
    │       ├── database.properties  # PostgreSQL connection
    │       ├── styles.css           # JavaFX styles
    │       └── sql/                 # DB migration scripts
    └── test/
        └── java/com/hotel/service/  # Unit tests (JUnit 5 + Mockito)
```

---

## 🗃️ Database Model

```
rooms               guests              users
─────────           ──────────          ──────────
id (PK)             id (PK)             id (PK)
number (UNIQUE)     first_name          username (UNIQUE)
type (ENUM)         last_name           email
capacity            document_number     password_hash
price_per_night     email               role (ENUM)
status (ENUM)       phone               is_active
is_active           is_active           created_at
created_at          created_at

reservations                activity_logs
──────────────              ──────────────
id (PK)                     id (PK)
room_id (FK → rooms)        user_id (FK → users, nullable)
guest_id (FK → guests)      http_method (ENUM)
user_id (FK → users)        resource
checkin_date                description
checkout_date               status_code
status (ENUM)               created_at
total_cost
iva_tax
created_at
```

---

## 🔐 Implemented Business Rules

| ID | Module | Rule |
|----|--------|-------|
| BR-001 | Rooms | Room number required |
| BR-002 | Rooms | Price per night > 0 |
| BR-003 | Rooms | Unique room number |
| BR-004 | Rooms | Capacity > 0 |
| BR-005 | Rooms | Room type required |
| BR-006 | Rooms | Do not deactivate if OCCUPIED |
| BR-001 | Guests | First name required |
| BR-002 | Guests | Last name required |
| BR-003 | Guests | Unique document number |
| BR-004 | Guests | Valid email format |
| BR-005 | Guests | Phone 7-15 digits |
| **BR-006** | **Guests** | **Do not deactivate with active reservations** |
| BR-001 | Reservations | Check-in < Check-out |
| BR-002 | Reservations | Room available |
| BR-003 | Reservations | Guest active |
| BR-004 | Reservations | No date overlap |
| BR-005 | Reservations | No check-out without prior check-in |
| BR-001 | Users | Username required |
| BR-002 | Users | Password minimum 8 characters |
| BR-003 | Users | Unique username |
| BR-004 | Users | Email required and valid |
| BR-005 | Users | Role required |

---

## 📤 Data Export

By clicking **"📤 Export CSV"** on the Dashboard:

- A **FileChooser** will open to let you pick a save location.
- **Report 1**: All rooms.
- **Report 2**: Active reservations (CHECKIN status).

---

## 📝 Logging System

Every operation generates a trace in **three simultaneous destinations**:

```
[2026-04-28 15:30:01] POST     /auth/login            → [200] User logged in: admin      | user:1
[2026-04-28 15:30:15] POST     /rooms                 → [201] Room 101 registered        | user:1
[2026-04-28 15:30:22] POST     /reservations          → [201] Check-in: room 1, guest 3  | user:2
[2026-04-28 15:31:05] PATCH    /reservations/1        → [200] Check-out completed        | user:2
[2026-04-28 15:32:00] POST     /rooms                 → [400] [ERROR] Room already exists | user:1
```

| Destination | Description |
|---------|-------------|
| **Console** | Real-time HTTP traces |
| **`app.log`** | Persistent file with `java.util.logging` |
| **`activity_logs` (DB)** | Historical record with user, method, resource, and status code |

---

## 🧪 Testing Summary

```bash
mvn test
```

| Suite | Tests | Result |
|-------|---------|-----------|
| `RoomServiceTest` | 10 | ✅ PASS |
| `GuestServiceTest` | 11 | ✅ PASS |
| `ReservationServiceTest` | 10 | ✅ PASS |
| `UserServiceTest` | 11 | ✅ PASS |
| **Total** | **42** | **✅ BUILD SUCCESS** |

---

## 🚨 Important Notes

> [!WARNING]
> The `database.properties` file contains real credentials. **Do not upload it to a public repository** without removing the passwords.

> [!NOTE]
> During unit tests (`mvn test`), the Logger displays warnings about `activity_logs not found`. This is **expected**: tests use H2 in-memory DB which doesn't have that table, but the system handles it gracefully.

> [!TIP]
> To reset the database to its initial state, manually execute `001_create_schema.sql` and `002_insert_test_data.sql` in your PostgreSQL client.
