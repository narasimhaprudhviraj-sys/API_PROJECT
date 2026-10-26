package com.example.onlinecourse.exception;

public class FacultyNotFoundException extends ResourceNotFoundException {
    public FacultyNotFoundException(String message) {
        super(message);
    }
}
