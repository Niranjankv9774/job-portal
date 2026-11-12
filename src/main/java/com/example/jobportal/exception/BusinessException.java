package com.example.jobportal.exception;

/** Thrown when a user does something the rules do not allow (duplicate email, bad file, ...). */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}