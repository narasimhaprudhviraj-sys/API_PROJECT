package com.example.onlinecourse.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralized REST Exception Handler demonstrating exception handling & validation.
 * Catches all domain, security, and business rule exceptions, returning standardized JSON responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        logger.warn("Resource not found: {}", ex.getMessage());
        ErrorDetails error = new ErrorDetails(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateRegistrationException.class)
    public ResponseEntity<ErrorDetails> handleDuplicateRegistrationException(
            DuplicateRegistrationException ex, WebRequest request) {
        logger.warn("Duplicate registration attempt: {}", ex.getMessage());
        ErrorDetails error = new ErrorDetails(
                HttpStatus.CONFLICT.value(),
                "Duplicate Registration",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CourseFullException.class)
    public ResponseEntity<ErrorDetails> handleCourseFullException(
            CourseFullException ex, WebRequest request) {
        logger.warn("Course full: {}", ex.getMessage());
        ErrorDetails error = new ErrorDetails(
                HttpStatus.CONFLICT.value(),
                "Course Seat Limit Exceeded",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler({ConcurrentSeatUpdateException.class, ObjectOptimisticLockingFailureException.class})
    public ResponseEntity<ErrorDetails> handleConcurrentSeatUpdateException(
            Exception ex, WebRequest request) {
        logger.warn("Concurrent course registration conflict: {}", ex.getMessage());
        ErrorDetails error = new ErrorDetails(
                HttpStatus.CONFLICT.value(),
                "Concurrent Seat Conflict",
                "Another student registered for the seat simultaneously. Please refresh and try again.",
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RegistrationClosedException.class)
    public ResponseEntity<ErrorDetails> handleRegistrationClosedException(
            RegistrationClosedException ex, WebRequest request) {
        logger.warn("Registration window closed: {}", ex.getMessage());
        ErrorDetails error = new ErrorDetails(
                HttpStatus.BAD_REQUEST.value(),
                "Registration Window Closed",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidCourseEligibilityException.class)
    public ResponseEntity<ErrorDetails> handleInvalidCourseEligibilityException(
            InvalidCourseEligibilityException ex, WebRequest request) {
        logger.warn("Course eligibility violation: {}", ex.getMessage());
        ErrorDetails error = new ErrorDetails(
                HttpStatus.BAD_REQUEST.value(),
                "Course Eligibility Restriction",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AcademicMismatchException.class)
    public ResponseEntity<ErrorDetails> handleAcademicMismatchException(
            AcademicMismatchException ex, WebRequest request) {
        logger.warn("Academic hierarchy mismatch: {}", ex.getMessage());
        ErrorDetails error = new ErrorDetails(
                HttpStatus.BAD_REQUEST.value(),
                "Academic Hierarchy Mismatch",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidRegistrationWindowException.class)
    public ResponseEntity<ErrorDetails> handleInvalidRegistrationWindowException(
            InvalidRegistrationWindowException ex, WebRequest request) {
        logger.warn("Invalid registration window: {}", ex.getMessage());
        ErrorDetails error = new ErrorDetails(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid Registration Window Configuration",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({UnauthorizedOperationException.class, AccessDeniedException.class})
    public ResponseEntity<ErrorDetails> handleAccessDeniedException(
            Exception ex, WebRequest request) {
        logger.warn("Unauthorized/Forbidden operation: {}", ex.getMessage());
        ErrorDetails error = new ErrorDetails(
                HttpStatus.FORBIDDEN.value(),
                "Access Denied",
                ex.getMessage() != null ? ex.getMessage() : "You do not have permission to perform this operation.",
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({InvalidCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<ErrorDetails> handleAuthenticationException(
            Exception ex, WebRequest request) {
        logger.warn("Authentication failure: {}", ex.getMessage());
        ErrorDetails error = new ErrorDetails(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                ex.getMessage() != null ? ex.getMessage() : "Invalid credentials or token expired.",
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        List<String> errors = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.add(fieldError.getField() + ": " + fieldError.getDefaultMessage());
        }

        logger.warn("Validation error on request to {}: {}", request.getDescription(false), errors);
        ErrorDetails error = new ErrorDetails(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "Input validation failed. Please check field requirements.",
                request.getDescription(false).replace("uri=", ""),
                errors
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDetails> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        logger.warn("Illegal argument: {}", ex.getMessage());
        ErrorDetails error = new ErrorDetails(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(
            Exception ex, WebRequest request) {
        logger.error("Unhandled server exception: ", ex);
        ErrorDetails error = new ErrorDetails(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                ex.getMessage() != null ? ex.getMessage() : "An unexpected server error occurred.",
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
