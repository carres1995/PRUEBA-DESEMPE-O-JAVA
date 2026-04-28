# 📘 Documentación Técnica — HotelNova

Este documento detalla la arquitectura, el flujo de datos y las funciones principales del sistema de gestión hotelera **HotelNova**.

---

## 1. Arquitectura del Sistema (MVC por Capas)

El proyecto sigue un patrón **MVC (Modelo-Vista-Controlador)** con una separación estricta de responsabilidades en 4 capas:

1.  **Vista (`com.hotel.view`)**: Desarrollada en JavaFX. No contiene lógica de negocio ni acceso a datos. Se comunica exclusivamente con el Controlador.
2.  **Controlador (`com.hotel.controller`)**: Actúa como puente. Recibe las interacciones del usuario y las delega al Servicio correspondiente.
3.  **Servicio (`com.hotel.service`)**: Es el "cerebro". Aquí reside la **Lógica de Negocio**, las validaciones (`BR-XXX`) y el manejo de **Transacciones** (commit/rollback).
4.  **DAO / Acceso a Datos (`com.hotel.dao`)**: Contiene la implementación de persistencia mediante **JDBC puro**. Se apoya en la clase `queries/` para mantener el SQL organizado.

---

## 2. Flujo de Datos (Ejemplo: Crear una Reserva)

Cuando un usuario hace clic en "Confirm Check-in":

1.  **View (`ReservationView`)**: Captura los IDs de habitación y huésped de los `ComboBox` y llama al controlador:
    `controller.handleCheckIn(roomId, guestId, checkIn, checkOut);`
2.  **Controller (`ReservationController`)**: Instancia el modelo `Reservation` y lo pasa al servicio:
    `reservationService.createReservation(reservation);`
3.  **Service (`ReservationService`)**:
    *   Inicia una transacción (`conn.setAutoCommit(false)`).
    *   Valida si la habitación está disponible.
    *   Valida que no haya solapamiento de fechas (`reservationDao.hasOverlap`).
    *   Registra la reserva en la BD.
    *   Actualiza el estado de la habitación a `OCCUPIED`.
    *   Realiza el `commit()` de la transacción.
4.  **DAO (`ReservationDao`)**: Ejecuta el `PreparedStatement` usando las constantes de `ReservationQueries`.

---

## 3. Funciones Principales y Lógica de Negocio

### 🔐 Autenticación y Seguridad
*   **BCrypt**: Se utiliza la librería `jBCrypt` en `PasswordHasher.java` para que las contraseñas nunca se guarden en texto plano.
*   **UserSession**: Un singleton que mantiene los datos del usuario logueado, permitiendo el control de acceso (RBAC) y el registro de auditoría.

### 🛎️ Gestión de Reservas (BR-004)
La lógica más compleja reside en evitar el solapamiento de fechas. El método `hasOverlap` en el DAO utiliza una consulta SQL optimizada:
```sql
SELECT COUNT(*) FROM reservations 
WHERE room_id = ? 
AND status NOT IN ('FINISHED', 'CANCELLED') 
AND checkin_date < ? AND checkout_date > ?
```

### 📊 Sistema de Logging (AppLogger)
Cada acción importante genera logs en tres destinos:
*   **Consola**: Simulación de trazas HTTP (`[POST] /rooms → [201]`).
*   **Archivo (`app.log`)**: Registro persistente para depuración.
*   **Base de Datos (`activity_logs`)**: Auditoría completa que incluye quién realizó la acción, qué recurso afectó y el código de estado.

### 📤 Exportación CSV (CsvExporter)
Utiliza la API `java.io` y `FileChooser` de JavaFX para permitir al usuario guardar reportes de habitaciones y reservas en cualquier ubicación de su computadora, formateando correctamente los datos para Excel.

---

## 4. Componentes Clave en el Código

| Clase | Función |
|-------|---------|
| `ConnectionFactory` | Centraliza la creación de conexiones JDBC usando `database.properties`. |
| `DatabaseInitializer` | Ejecuta automáticamente los scripts SQL en `resources/sql` al iniciar la app. |
| `MainView` | Clase principal que gestiona el `Stage` y permite el intercambio de escenas (`setScene`). |
| `ConfigUtil` | Lee parámetros de negocio como el IVA (`vat`) desde `config.properties`. |
| `RoomQueries` | Centraliza el SQL de habitaciones, incluyendo la construcción dinámica de filtros. |

---

## 5. Manejo de Excepciones
El sistema utiliza excepciones personalizadas (ej. `RoomException`) para atrapar errores de la base de datos y convertirlos en mensajes amigables para el usuario, que se muestran a través de `Alert` en la interfaz.

---

## 6. Pruebas Unitarias
Se utiliza **JUnit 5** y **Mockito** para probar la capa de Servicios de forma aislada.
*   **Mocks**: Se simula el comportamiento de los DAOs para probar la lógica de negocio sin depender de una base de datos real.
*   **H2 Database**: Los tests de integración ligeros utilizan una base de datos en memoria.
