package com.restaurant.app.common;

import org.springframework.http.HttpStatus;

/** Exception thrown when authentication fails. Results in HTTP 401 response. */
public class UnauthorizedException extends AppException {

    public UnauthorizedException(String message) {
        super(
                HttpStatus.UNAUTHORIZED,
                "https://errors.restaurant.app/unauthorized",
                "Unauthorized",
                message);
    }
}
