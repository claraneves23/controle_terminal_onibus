package com.controle.terminal.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends ApiException {

    public BusinessException(String message) {
        super(HttpStatus.CONFLICT, "BUSINESS_RULE_VIOLATION", message);
    }

    public BusinessException(String code, String message) {
        super(HttpStatus.CONFLICT, code, message);
    }
}
