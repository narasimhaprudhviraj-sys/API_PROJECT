package com.example.onlinecourse.exception;

/**
 * Exception thrown when a registration window configuration is invalid.
 * Examples:
 * - End date/time is on or before start date/time
 * - An active window already exists for the same semester
 * - Overlapping active registration windows for the same semester
 */
public class InvalidRegistrationWindowException extends RuntimeException {
    public InvalidRegistrationWindowException(String message) {
        super(message);
    }
}
