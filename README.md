#  HotelNova — Sistema de Gestión Hotelera

> **Prueba de Desempeño — Módulo 5.1 Java SE**  
> Java SE + JavaFX + JDBC + Arquitectura por Capas + Archivos + Excepciones + Pruebas Unitarias

---

##  Datos del Coder

| Campo | Información |
|-------|-------------|
| **Nombre** | [Tu Nombre Completo] |
| **Clan** | [Tu Clan] |
| **Correo** | [tu.correo@riwi.io] |
| **Documento** | [Tu Número de Documento] |

---

##  Descripción General del Sistema

**HotelNova** es un sistema de gestión hotelera desarrollado en **Java SE 17+** con interfaz gráfica en **JavaFX**, persistencia con **JDBC puro** y arquitectura **MVC por capas estrictas**. Resuelve los problemas operativos de HotelNova:

| Problema Original | Solución Implementada |
|------------------|-----------------------|
| Duplicidad de reservas por IDs incorrectos | Validación de solapamiento de fechas + UNIQUE constraints |
| Inconsistencias en disponibilidad | Transacciones JDBC: Check-in/out atómicos |
| Falta de control de usuarios y roles | Autenticación con BCrypt + RBAC (ADMIN/RECEPTIONIST) |
| Check-in/out incompletos o con errores | Flujo transaccional con validaciones de negocio |
| Pérdida de información | Logs en DB, archivo `app.log` y exportación CSV |

---

## 🛠️ Stack Tecnológico

| Componente | Tecnología |
|------------|-----------|
| Lenguaje | Java 17+ |
| Interfaz gráfica | JavaFX 21 |
| Build Tool | Apache Maven |
| Base de datos (producción) | PostgreSQL (Supabase) |
| Base de datos (pruebas) | H2 In-Memory |
| Acceso a datos | Pure JDBC (`java.sql`) |
| Hash de contraseñas | jBCrypt |
| Pruebas unitarias | JUnit 5 + Mockito |
| Arquitectura | MVC por Capas (Controller → Service → DAO → DB) |

---

##  Soluciones a los Requisitos del Cliente

### 1 Gestión de Habitaciones
-  Registro de habitaciones con: `id`, `numero`, `tipo`, `capacidad`, `precioPorNoche`, `estado`, `isActiva`, `createdAt`
-  Edición y actualización de habitaciones existentes
-  Validación de número de habitación único (`BR-003`)
-  Listado y filtro por tipo o estado desde la base de datos
-  Tabla visual con columnas alineadas en la UI de JavaFX
-  Inventario dinámico: números de habitación cargados desde `room_inventory`

### 2 Gestión de Usuarios y Autenticación
-  Login con validación de credenciales y roles (`ADMIN` / `RECEPTIONIST`)
-  Contraseñas almacenadas con **BCrypt hash** (nunca en texto plano)
-  CRUD completo de usuarios con campo `email` obligatorio
-  Activación/desactivación de usuarios (toggle)
-  Logs HTTP en consola: `[POST] /auth/login → [200]`
-  Restricción de menú "Manage Users" solo para `ADMIN`

### 3 Interfaz Gráfica (JavaFX)
-  Single-Stage con Scene Manager (un solo Stage, múltiples Scenes)
-  Dashboard central con navegación a todos los módulos
-  Menús por módulo: Habitaciones, Huéspedes, Usuarios, Reservas, Exportaciones
-  Confirmaciones y mensajes de éxito/error con `Alert`
-  Tablas con columnas completas y políticas de redimensionamiento
-  Menús contextuales (clic derecho) para operaciones de estado

### 4 CRUD + JDBC + Transacciones
-  Interfaces DAO definidas en `dao/interfaces/`
-  Implementaciones JDBC con `PreparedStatement` + casting PostgreSQL (`::room_type`)
-  **Check-in**: `setAutoCommit(false)` → insertar reserva → actualizar habitación a `OCUPADA` → `commit()` / `rollback()`
-  **Check-out**: actualizar reserva a `COMPLETED` → calcular costo total + IVA → actualizar habitación a `DISPONIBLE` → `commit()`
-  `try-with-resources` en todas las conexiones y statements

### 5 Manejo de Archivos
-  `config.properties` con: `vat=0.19`, `checkInHour=15`, `checkOutHour=12`
-  `database.properties` con: `db.url`, `db.user`, `db.password`, `db.driver`
-  Exportación a CSV:
  - `exports/habitaciones_export_YYYYMMDD_HHmmss.csv` — listado completo
  - `exports/reservas_activas_YYYYMMDD_HHmmss.csv` — solo reservas activas
-  Registro de actividad en `app.log` con `java.util.logging`

### 6 Excepciones y Validaciones
-  Excepciones personalizadas: `RoomException`, `GuestException`, `ReservationException`, `UserException`, `AuthenticationException`
-  Validaciones de negocio implementadas:
  - Número de habitación único
  - Habitación disponible para reserva
  - Huésped activo para reserva
  - Fechas válidas: `checkIn < checkOut`
  - Sin solapamiento de reservas para la misma habitación
  - No se permite check-out sin check-in previo
  - **BR-006**: No se puede desactivar un huésped con reservas activas
-  Errores mostrados en `Alert` de JavaFX
-  Detalles guardados en `app.log` y tabla `activity_logs`

### 7 Pruebas Unitarias (JUnit 5)
-  **42 pruebas** ejecutándose con `mvn test — BUILD SUCCESS`
-  `RoomServiceTest` (10 pruebas): número único, disponibilidad, tipos, precios
-  `GuestServiceTest` (11 pruebas): documento único, email, teléfono, BR-006
-  `ReservationServiceTest` (10 pruebas): fechas, solapamiento, check-in/out, costo con IVA
-  `UserServiceTest` (11 pruebas): credenciales, roles, BCrypt, email

---

##  Requisitos Previos

| Herramienta | Versión Mínima | Verificación |
|-------------|---------------|-------------|
| Java JDK | 17+ | `java -version` |
| Apache Maven | 3.8+ | `mvn -version` |
| PostgreSQL | 14+ (o Supabase) | Acceso a BD remota incluido |
| Git | Cualquiera | `git --version` |

>  **No se necesita instalar PostgreSQL localmente.** La aplicación se conecta a una instancia en **Supabase** ya configurada.

---

##  Pasos de Configuración y Ejecución

### 1. Clonar el repositorio

```bash
git clone https://github.com/[tu-usuario]/hotelNova.git
cd hotelNova
```

### 2. Verificar la configuración de la base de datos

El archivo `src/main/resources/database.properties` ya contiene la conexión a Supabase:

```properties
db.url      = jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres?currentSchema=hotel
db.user     = [usuario]
db.password = [contraseña]
db.driver   = org.postgresql.Driver
```

>  Si usas tu propia BD PostgreSQL, actualiza estos valores con tus credenciales.

### 3. Verificar parámetros de negocio

Archivo `src/main/resources/config.properties`:

```properties
vat=0.19          # IVA del 19% aplicado al costo total de la estadía
checkInHour=15    # Hora oficial de check-in (3:00 PM)
checkOutHour=12   # Hora oficial de check-out (12:00 PM)
```

### 4. Ejecutar las pruebas unitarias

```bash
mvn test
```

**Resultado esperado:**
```
Tests run: 42, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### 5. Ejecutar la aplicación

```bash
mvn javafx:run
```

**Al iniciar, el sistema automáticamente:**
1. Conecta a PostgreSQL
2. Ejecuta los scripts SQL de inicialización (`sql/*.sql`)
3. Muestra la pantalla de Login

### 6. Credenciales de acceso por defecto

| Usuario | Contraseña | Rol |
|---------|-----------|-----|
| `admin` | `admin123` | ADMIN |

---

## 🗂️ Estructura del Proyecto

```
hotelNova/
├── pom.xml                          # Configuración Maven + dependencias
├── README.md                        # Este archivo
├── app.log                          # Log de actividad (generado al ejecutar)
├── exports/                         # Archivos CSV exportados (generados en runtime)
│
└── src/
    ├── main/
    │   ├── java/com/hotel/
    │   │   ├── config/              # Configuración DB e inicialización
    │   │   │   ├── AppConfig.java
    │   │   │   ├── ConnectionFactory.java
    │   │   │   └── DatabaseInitializer.java
    │   │   ├── controller/          # Capa de entrada (MVC Controller)
    │   │   │   ├── AuthenticationController.java
    │   │   │   ├── GuestController.java
    │   │   │   ├── ReservationController.java
    │   │   │   ├── RoomController.java
    │   │   │   └── UserController.java
    │   │   ├── dao/                 # Capa de acceso a datos (JDBC)
    │   │   │   ├── interfaces/      # Contratos DAO
    │   │   │   ├── queries/         # Constantes SQL
    │   │   │   ├── ActivityLogDao.java
    │   │   │   ├── GuestDao.java
    │   │   │   ├── ReservationDao.java
    │   │   │   ├── RoomDao.java
    │   │   │   └── UserDao.java
    │   │   ├── exception/           # Excepciones personalizadas
    │   │   ├── model/               # Entidades del dominio
    │   │   ├── service/             # Lógica de negocio + transacciones
    │   │   ├── util/                # Utilidades transversales
    │   │   │   ├── AppLogger.java   # Logger: consola + archivo + BD
    │   │   │   ├── ConfigUtil.java
    │   │   │   ├── CsvExporter.java # Exportador CSV
    │   │   │   ├── PasswordHasher.java
    │   │   │   └── UserSession.java
    │   │   └── view/                # Capa de presentación (JavaFX)
    │   │       ├── MainView.java    # Stage Manager central
    │   │       ├── LoginView.java
    │   │       ├── GuestView.java
    │   │       ├── ReservationView.java
    │   │       ├── RoomView.java
    │   │       └── UserView.java
    │   └── resources/
    │       ├── config.properties    # Parámetros de negocio (IVA, horarios)
    │       ├── database.properties  # Conexión a PostgreSQL
    │       ├── styles.css           # Estilos JavaFX
    │       ├── doc/spec/            # Especificaciones técnicas por módulo
    │       └── sql/                 # Scripts de migración de BD
    │           ├── 001_create_schema.sql
    │           ├── 002_insert_test_data.sql
    │           ├── 003_room_inventory.sql
    │           └── 004_fix_enums.sql
    └── test/
        └── java/com/hotel/service/  # Pruebas unitarias (JUnit 5 + Mockito)
            ├── GuestServiceTest.java
            ├── ReservationServiceTest.java
            ├── RoomServiceTest.java
            └── UserServiceTest.java
```

---

## 🗃️ Modelo de Base de Datos

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

## 🔐 Reglas de Negocio Implementadas

| ID | Módulo | Regla |
|----|--------|-------|
| BR-001 | Habitaciones | Número de habitación requerido |
| BR-002 | Habitaciones | Precio por noche > 0 |
| BR-003 | Habitaciones | Número de habitación único |
| BR-004 | Habitaciones | Capacidad > 0 |
| BR-005 | Habitaciones | Tipo de habitación requerido |
| BR-006 | Habitaciones | No desactivar si está OCUPADA |
| BR-001 | Huéspedes | Nombre requerido |
| BR-002 | Huéspedes | Apellido requerido |
| BR-003 | Huéspedes | Número de documento único |
| BR-004 | Huéspedes | Formato de email válido |
| BR-005 | Huéspedes | Teléfono 7-15 dígitos |
| **BR-006** | **Huéspedes** | **No desactivar con reservas activas** |
| BR-001 | Reservas | Check-in < Check-out |
| BR-002 | Reservas | Habitación disponible |
| BR-003 | Reservas | Huésped activo |
| BR-004 | Reservas | Sin solapamiento de fechas |
| BR-005 | Reservas | No check-out sin check-in previo |
| BR-001 | Usuarios | Username requerido |
| BR-002 | Usuarios | Contraseña mínimo 8 caracteres |
| BR-003 | Usuarios | Username único |
| BR-004 | Usuarios | Email requerido y válido |
| BR-005 | Usuarios | Rol requerido |

---

## 📤 Exportación de Datos

Al presionar **"📤 Export CSV"** en el Dashboard:

- **`exports/habitaciones_export_<timestamp>.csv`**: todas las habitaciones
- **`exports/reservas_activas_<timestamp>.csv`**: reservas con estado CHECKIN

Los archivos se crean en la carpeta `exports/` dentro del directorio del proyecto.

---

## 📝 Sistema de Logging

Cada operación genera una traza en **tres destinos simultáneos**:

```
[2026-04-28 15:30:01] POST     /auth/login            → [200] User logged in: admin      | user:1
[2026-04-28 15:30:15] POST     /rooms                 → [201] Room 101 registered        | user:1
[2026-04-28 15:30:22] POST     /reservations          → [201] Check-in: room 1, guest 3  | user:2
[2026-04-28 15:31:05] PATCH    /reservations/1        → [200] Check-out completed        | user:2
[2026-04-28 15:32:00] POST     /rooms                 → [400] [ERROR] Room already exists | user:1
```

| Destino | Descripción |
|---------|-------------|
| **Consola** | Trazas HTTP en tiempo real |
| **`app.log`** | Archivo persistente con `java.util.logging` |
| **`activity_logs` (BD)** | Registro histórico con usuario, método, recurso y código de estado |

---

## 🧪 Resumen de Pruebas

```bash
mvn test
```

| Suite | Pruebas | Resultado |
|-------|---------|-----------|
| `RoomServiceTest` | 10 | ✅ PASS |
| `GuestServiceTest` | 11 | ✅ PASS |
| `ReservationServiceTest` | 10 | ✅ PASS |
| `UserServiceTest` | 11 | ✅ PASS |
| **Total** | **42** | **✅ BUILD SUCCESS** |

---

## 🚨 Notas Importantes

> [!WARNING]
> El archivo `database.properties` contiene credenciales reales. **No lo subas a un repositorio público** sin remover las contraseñas.

> [!NOTE]
> Durante las pruebas unitarias (`mvn test`), el Logger muestra warnings de `activity_logs not found`. Esto es **esperado**: los tests usan H2 en memoria que no tiene esa tabla, pero el sistema los ignora correctamente sin fallar.

> [!TIP]
> Para resetear la base de datos a su estado inicial, ejecuta manualmente `001_create_schema.sql` y `002_insert_test_data.sql` en tu cliente PostgreSQL.
