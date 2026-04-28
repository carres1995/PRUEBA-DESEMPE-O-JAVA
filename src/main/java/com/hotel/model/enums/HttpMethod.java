package com.hotel.model.enums;

/**
 * Represents HTTP methods used for activity logging.
 * Maps to the {@code http_method} PostgreSQL ENUM in the database.
 */
public enum HttpMethod {
    GET,
    POST,
    PUT,
    PATCH,
    DELETE
}
