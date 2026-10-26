package com.example.onlinecourse.exception;

public class SectionNotFoundException extends ResourceNotFoundException {
    public SectionNotFoundException(String message) {
        super(message);
    }
}
