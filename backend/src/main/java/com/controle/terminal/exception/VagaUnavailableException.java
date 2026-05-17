package com.controle.terminal.exception;

import com.controle.terminal.domain.enums.StatusVaga;
import org.springframework.http.HttpStatus;

public class VagaUnavailableException extends ApiException {

    public VagaUnavailableException(String codigoVaga, StatusVaga statusAtual) {
        super(HttpStatus.CONFLICT,
              "VAGA_UNAVAILABLE",
              "A vaga '%s' nao esta livre. Status atual: %s."
                      .formatted(codigoVaga, statusAtual.name()));
    }
}
