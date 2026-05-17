package com.controle.terminal.exception;

import com.controle.terminal.domain.enums.StatusOperacao;
import org.springframework.http.HttpStatus;

public class OperacaoNotFinishedException extends ApiException {

    public OperacaoNotFinishedException(Long operacaoId, StatusOperacao statusAtual) {
        super(HttpStatus.CONFLICT,
              "OPERACAO_NOT_FINISHED",
              "Nao e possivel fazer checkout da operacao %d: ela ainda nao foi finalizada (status atual: %s)."
                      .formatted(operacaoId, statusAtual.name()));
    }
}
