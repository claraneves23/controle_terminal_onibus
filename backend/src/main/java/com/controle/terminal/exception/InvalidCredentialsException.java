package com.controle.terminal.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends ApiException {

    public InvalidCredentialsException() {
        super(HttpStatus.UNAUTHORIZED,
              "INVALID_CREDENTIALS",
              "Email ou senha incorretos.");
    }
}
