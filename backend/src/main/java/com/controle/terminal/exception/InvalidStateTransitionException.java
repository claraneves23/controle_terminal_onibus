package com.controle.terminal.exception;

import org.springframework.http.HttpStatus;

public class InvalidStateTransitionException extends ApiException {

    public InvalidStateTransitionException(String recurso, Object id, Enum<?> statusAtual, String acao) {
        super(HttpStatus.CONFLICT,
              "INVALID_STATE_TRANSITION",
              "Nao e possivel %s %s %s: status atual e %s."
                      .formatted(acao, recurso.toLowerCase(), id, statusAtual.name()));
    }
}
