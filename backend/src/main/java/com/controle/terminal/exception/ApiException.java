package com.controle.terminal.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;
    private final transient List<ApiFieldError> details;

    protected ApiException(HttpStatus status, String code, String message) {
        this(status, code, message, null);
    }

    protected ApiException(HttpStatus status, String code, String message, List<ApiFieldError> details) {
        super(message);
        this.status = status;
        this.code = code;
        this.details = details;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public List<ApiFieldError> getDetails() {
        return details;
    }
}
