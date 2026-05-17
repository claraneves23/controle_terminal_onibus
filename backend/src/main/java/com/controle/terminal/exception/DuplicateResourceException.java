package com.controle.terminal.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends ApiException {

    public DuplicateResourceException(String recurso, String campo, Object valor) {
        super(HttpStatus.CONFLICT,
              "DUPLICATE_RESOURCE",
              "Ja existe um(a) %s com %s '%s'.".formatted(recurso, campo, valor));
    }
}
