package com.controle.terminal.exception;

import com.controle.terminal.domain.enums.StatusIncidente;
import org.springframework.http.HttpStatus;

public class IncidentAlreadyClosedException extends ApiException {

    public IncidentAlreadyClosedException(Long incidenteId, StatusIncidente statusAtual) {
        super(HttpStatus.CONFLICT,
              "INCIDENT_ALREADY_CLOSED",
              "O incidente %d ja esta encerrado (status atual: %s)."
                      .formatted(incidenteId, statusAtual.name()));
    }
}
