package com.controle.terminal.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String recurso, Object id) {
        super(HttpStatus.NOT_FOUND,
              "RESOURCE_NOT_FOUND",
              "%s com id %s nao foi encontrado.".formatted(recurso, id));
    }

    public ResourceNotFoundException(String recurso, String campo, Object valor) {
        super(HttpStatus.NOT_FOUND,
              "RESOURCE_NOT_FOUND",
              "%s com %s '%s' nao foi encontrado.".formatted(recurso, campo, valor));
    }
}
