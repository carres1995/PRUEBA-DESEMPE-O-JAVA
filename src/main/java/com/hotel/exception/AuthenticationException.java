package com.hotel.exception;

/**
 * Exception thrown for authentication failures (e.g., invalid credentials, inactive user).
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
