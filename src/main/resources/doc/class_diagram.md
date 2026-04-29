# Diagrama de Clases - Proyecto Hotel

Este diagrama representa la arquitectura del sistema de gestión hotelera, siguiendo el patrón **MVC (Modelo-Vista-Controlador)** y utilizando el patrón **DAO (Data Access Object)** para la persistencia.

```mermaid
classDiagram
    %% --- RELACIONES DE HERENCIA Y REALIZACIÓN ---
    IGenericDao <|-- IRoomDao : extends
    IGenericDao <|-- IGuestDao : extends
    IGenericDao <|-- IReservationDao : extends
    IGenericDao <|-- IUserDao : extends
    
    IGenericDao <|.. AbstractDao : implements
    AbstractDao <|-- RoomDao : extends
    AbstractDao <|-- GuestDao : extends
    AbstractDao <|-- ReservationDao : extends
    AbstractDao <|-- UserDao : extends
    
    IRoomDao <|.. RoomDao : implements
    IGuestDao <|.. GuestDao : implements
    IReservationDao <|.. ReservationDao : implements
    IUserDao <|.. UserDao : implements

    %% --- MODELOS ---
    class Room {
        -Long id
        -String number
        -RoomType type
        -int capacity
        -double pricePerNight
        -RoomStatus status
        -boolean isActive
        -LocalDateTime createdAt
    }

    class Guest {
        -Long id
        -String firstName
        -String lastName
        -String documentNumber
        -String email
        -String phone
        -boolean isActive
        -LocalDateTime createdAt
    }

    class Reservation {
        -Long id
        -Long roomId
        -Long guestId
        -Long userId
        -LocalDate checkIn
        -LocalDate checkOut
        -ReservationStatus status
        -double totalCost
        -LocalDateTime createdAt
    }

    class User {
        -Long id
        -String username
        -String email
        -String passwordHash
        -Role role
        -boolean isActive
        -LocalDateTime createdAt
    }

    class ActivityLog {
        -Long id
        -HttpMethod method
        -String endpoint
        -String description
        -int statusCode
        -Long userId
        -LocalDateTime createdAt
    }

    %% --- DAO INTERFACES ---
    class IGenericDao {
        <<interface>>
        +insert(Connection, T)
        +update(Connection, T)
        +findById(Connection, K)
        +listAll(Connection)
        +delete(Connection, K)
    }

    class IRoomDao {
        <<interface>>
        +existsByNumber(Connection, String)
        +listByTypeAndStatus(Connection, RoomType, RoomStatus)
    }

    %% --- SERVICES ---
    class RoomService {
        -IRoomDao roomDao
        +register(Room)
        +update(Room)
        +deactivate(Room)
    }

    class ReservationService {
        -IReservationDao reservationDao
        -IRoomDao roomDao
        -IGuestDao guestDao
        +createReservation(Reservation)
        +cancelReservation(Long)
        +checkIn(Long)
        +checkOut(Long)
    }

    %% --- CONTROLLERS ---
    class RoomController {
        -RoomService roomService
        +handleRequest()
    }

    class ReservationController {
        -ReservationService reservationService
        +handleRequest()
    }

    %% --- RELACIONES DE USO ---
    RoomService --> IRoomDao : uses
    ReservationService --> IReservationDao : uses
    ReservationService --> IRoomDao : uses
    ReservationService --> IGuestDao : uses
    
    RoomController --> RoomService : uses
    ReservationController --> ReservationService : uses
    
    Reservation "1" --> "1" Room : references
    Reservation "1" --> "1" Guest : references
    Reservation "1" --> "1" User : created by
    ActivityLog "1" --> "1" User : associated with
```

## Detalles de la Arquitectura

1.  **Capa de Modelo**: Contiene las entidades POJO que representan las tablas de la base de datos (`Room`, `Guest`, `Reservation`, `User`).
2.  **Capa DAO (Persistencia)**:
    *   **IGenericDao**: Interfaz genérica con operaciones CRUD básicas.
    *   **AbstractDao**: Implementación base que reduce la duplicación de código JDBC.
    *   **Implementaciones Específicas**: Manejan la lógica SQL particular de cada entidad.
3.  **Capa de Servicio**: Contiene la lógica de negocio, validaciones y gestión de transacciones.
4.  **Capa de Controlador**: Gestiona la interacción con el usuario y delega las acciones a los servicios correspondientes.
5.  **Enums**: El sistema utiliza enums para estados y tipos (`RoomStatus`, `RoomType`, `ReservationStatus`, `Role`), garantizando la integridad de los datos.
