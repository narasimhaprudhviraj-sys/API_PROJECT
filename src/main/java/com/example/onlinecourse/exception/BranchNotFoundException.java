package com.example.onlinecourse.exception;

public class BranchNotFoundException extends ResourceNotFoundException {
    public BranchNotFoundException(String message) {
        super(message);
    }
}
