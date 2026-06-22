package com.restaurant.app.common;

import org.springframework.http.HttpStatus;

/** Exception thrown when a requested resource is not found. Results in HTTP 404 response. */
public class NotFoundException extends AppException {

    public NotFoundException(String resource, String id) {
        super(
                HttpStatus.NOT_FOUND,
                "https://errors.restaurant.app/not-found",
                "Resource Not Found",
                String.format("%s with id '%s' not found", resource, id));
    }

    public NotFoundException(String resource, Object id) {
        this(resource, String.valueOf(id));
    }

    public NotFoundException(String message) {
        super(
                HttpStatus.NOT_FOUND,
                "https://errors.restaurant.app/not-found",
                "Resource Not Found",
                message);
    }
}
