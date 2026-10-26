package com.example.onlinecourse.exception;

public class RegistrationNotFoundException extends ResourceNotFoundException {
    public RegistrationNotFoundException(String message) {
        super(message);
    }
}
