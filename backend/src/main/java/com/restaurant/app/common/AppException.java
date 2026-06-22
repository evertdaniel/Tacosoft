package com.restaurant.app.common;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/** Base exception for application errors. All domain exceptions extend this class. */
public abstract class AppException extends RuntimeException {

    private final ProblemDetail problemDetail;

    protected AppException(HttpStatus status, String type, String title, String detail) {
        super(detail);
        this.problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        this.problemDetail.setType(URI.create(type));
        this.problemDetail.setTitle(title);
    }

    protected AppException(HttpStatus status, String type, String detail) {
        this(status, type, status.getReasonPhrase(), detail);
    }

    public ProblemDetail getProblemDetail() {
        return problemDetail;
    }

    @Override
    public String getMessage() {
        return problemDetail.getDetail();
    }
}
