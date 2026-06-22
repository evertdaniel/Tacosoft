package com.restaurant.app.common;

import org.springframework.http.HttpStatus;

/** Exception thrown when user lacks permission for an action. Results in HTTP 403 response. */
public class ForbiddenException extends AppException {

    public ForbiddenException(String message) {
        super(
                HttpStatus.FORBIDDEN,
                "https://errors.restaurant.app/forbidden",
                "Forbidden",
                message);
    }
}
