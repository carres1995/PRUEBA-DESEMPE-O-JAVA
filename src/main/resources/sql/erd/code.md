CREATE TYPE room_type AS ENUM (
    'SINGLE', 'DOUBLE', 'SUITE', 'FAMILY'
);

CREATE TYPE room_status AS ENUM (
    'AVAILABLE', 'OCCUPIED'
);

CREATE TYPE reservation_status AS ENUM (
    'ACTIVE', 'FINISHED', 'CANCELLED'
);

CREATE TYPE user_role AS ENUM (
    'ADMIN', 'RECEPTIONIST'
);

CREATE TYPE http_method AS ENUM (
    'GET', 'POST', 'PUT', 'PATCH', 'DELETE'
);

CREATE TABLE rooms (
    id               BIGSERIAL      NOT NULL,
    number           VARCHAR(10)    NOT NULL,
    type             room_type      NOT NULL,
    capacity         INTEGER        NOT NULL,
    price_per_night  NUMERIC(10,2)  NOT NULL,
    status           room_status    NOT NULL DEFAULT 'AVAILABLE',
    is_active        BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_rooms           PRIMARY KEY (id),
    CONSTRAINT uq_room_number     UNIQUE (number),
    CONSTRAINT chk_capacity       CHECK (capacity > 0),
    CONSTRAINT chk_price          CHECK (price_per_night > 0)
);


CREATE TABLE guests (
    id                  BIGSERIAL    NOT NULL,
    first_name          VARCHAR(100) NOT NULL,
    last_name           VARCHAR(100) NOT NULL,
    email               VARCHAR(150) NOT NULL,
    phone               VARCHAR(20),
    document_id         VARCHAR(30)  NOT NULL,
    is_active           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_guests              PRIMARY KEY (id),
    CONSTRAINT uq_guest_email         UNIQUE (email),
    CONSTRAINT uq_guest_document      UNIQUE (document_id)
);


CREATE TABLE users (
    id            BIGSERIAL    NOT NULL,
    username      VARCHAR(60)  NOT NULL,
    email         VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          user_role    NOT NULL DEFAULT 'RECEPTIONIST',
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_users          PRIMARY KEY (id),
    CONSTRAINT uq_user_username  UNIQUE (username),
    CONSTRAINT uq_user_email     UNIQUE (email)
);


CREATE TABLE reservations (
    id              BIGSERIAL         NOT NULL,
    room_id         BIGINT            NOT NULL,
    guest_id        BIGINT            NOT NULL,
    user_id         BIGINT            NOT NULL,
    checkin_date    DATE              NOT NULL,
    checkout_date   DATE              NOT NULL,
    status          reservation_status NOT NULL DEFAULT 'ACTIVE',
    total_cost      NUMERIC(12,2),
    vat_tax         NUMERIC(12,2),
    created_at      TIMESTAMPTZ       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ       NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_reservations   PRIMARY KEY (id),
    CONSTRAINT chk_dates        CHECK (checkout_date > checkin_date),

    CONSTRAINT fk_reservation_room
        FOREIGN KEY (room_id)
        REFERENCES rooms(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_reservation_guest
        FOREIGN KEY (guest_id)
        REFERENCES guests(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_reservation_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);


CREATE INDEX idx_reservations_room_dates
    ON reservations (room_id, checkin_date, checkout_date, status);


CREATE TABLE activity_logs (
    id           BIGSERIAL    NOT NULL,
    user_id      BIGINT,
    http_method  http_method  NOT NULL,
    resource     VARCHAR(200) NOT NULL,
    description  VARCHAR(500),
    status_code  INTEGER      NOT NULL DEFAULT 200,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_activity_logs PRIMARY KEY (id),

    CONSTRAINT fk_log_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);


CREATE OR REPLACE FUNCTION fn_set_updated_at()
RETURNS TRIGGER
LANGUAGE plpgsql AS
$$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_rooms_updated_at
    BEFORE UPDATE ON rooms
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TRIGGER trg_guests_updated_at
    BEFORE UPDATE ON guests
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TRIGGER trg_reservations_updated_at
    BEFORE UPDATE ON reservations
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

INSERT INTO users (username, email, password_hash, role)
VALUES (
    'admin',
    'admin@hotelnova.com',
    '$2a$12$REPLACE_WITH_REAL_BCRYPT_HASH',
    'ADMIN'
);