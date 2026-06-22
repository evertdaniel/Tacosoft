package com.restaurant.app.common;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a request conflicts with current state. Results in HTTP 409 response. Used
 * for invalid state transitions, duplicate resources, etc.
 */
public class ConflictException extends AppException {

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, "https://errors.restaurant.app/conflict", "Conflict", message);
    }
}
