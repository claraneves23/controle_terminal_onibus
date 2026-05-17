package com.controle.terminal.exception;

import com.controle.terminal.domain.enums.StatusDoca;
import org.springframework.http.HttpStatus;

public class DocaUnavailableException extends ApiException {

    public DocaUnavailableException(String codigoDoca, StatusDoca statusAtual) {
        super(HttpStatus.CONFLICT,
              "DOCA_UNAVAILABLE",
              "A doca '%s' nao esta disponivel. Status atual: %s."
                      .formatted(codigoDoca, statusAtual.name()));
    }
}
