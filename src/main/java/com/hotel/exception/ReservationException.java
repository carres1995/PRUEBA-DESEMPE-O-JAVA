package com.hotel.exception;

/**
 * Exception thrown when a reservation business rule is violated.
 */
public class ReservationException extends RuntimeException {
    public ReservationException(String message) {
        super(message);
    }

    public ReservationException(String message, Throwable cause) {
        super(message, cause);
    }
}
