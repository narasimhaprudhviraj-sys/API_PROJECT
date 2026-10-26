package com.example.onlinecourse.exception;

public class SemesterNotFoundException extends ResourceNotFoundException {
    public SemesterNotFoundException(String message) {
        super(message);
    }
}
