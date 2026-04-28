package com.hotel.exception;

public class GuestException extends RuntimeException {

    public GuestException(String message) {
        super(message);
    }

    public GuestException(String message, Throwable cause) {
        super(message, cause);
    }
}
