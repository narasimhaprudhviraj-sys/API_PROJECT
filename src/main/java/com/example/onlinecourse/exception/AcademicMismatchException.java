package com.example.onlinecourse.exception;

/**
 * Exception thrown when an academic hierarchy constraint is violated.
 * Examples:
 * - MinorStream branch does not match Course branch
 * - Section branch does not match MinorStream branch
 * - Student branch does not match MinorStream or Section branch
 */
public class AcademicMismatchException extends RuntimeException {
    public AcademicMismatchException(String message) {
        super(message);
    }
}
