package com.controle.terminal.dto.response;

import com.controle.terminal.domain.entity.Estacionamento;
import com.controle.terminal.domain.enums.StatusEstacionamento;

public record EstacionamentoResponse(
        Long id,
        Long terminalId,
        String terminalNome,
        String nome,
        Integer capacidade,
        StatusEstacionamento status
) {
    public static EstacionamentoResponse from(Estacionamento e) {
        return new EstacionamentoResponse(
                e.getId(),
                e.getTerminal() != null ? e.getTerminal().getId() : null,
                e.getTerminal() != null ? e.getTerminal().getNome() : null,
                e.getNome(),
                e.getCapacidade(),
                e.getStatus()
        );
    }
}
