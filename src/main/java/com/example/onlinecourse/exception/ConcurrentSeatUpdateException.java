package com.example.onlinecourse.exception;

/**
 * Exception thrown when a concurrent course registration conflict occurs
 * due to optimistic locking or simultaneous seat reservation race conditions.
 */
public class ConcurrentSeatUpdateException extends RuntimeException {
    public ConcurrentSeatUpdateException(String message) {
        super(message);
    }

    public ConcurrentSeatUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
