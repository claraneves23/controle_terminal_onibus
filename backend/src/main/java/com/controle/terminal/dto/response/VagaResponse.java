package com.controle.terminal.dto.response;

import com.controle.terminal.domain.entity.VagaEstacionamento;
import com.controle.terminal.domain.enums.StatusVaga;

public record VagaResponse(
        Long id,
        Long estacionamentoId,
        String estacionamentoNome,
        String codigo,
        StatusVaga status
) {
    public static VagaResponse from(VagaEstacionamento v) {
        return new VagaResponse(
                v.getId(),
                v.getEstacionamento() != null ? v.getEstacionamento().getId() : null,
                v.getEstacionamento() != null ? v.getEstacionamento().getNome() : null,
                v.getCodigo(),
                v.getStatus()
        );
    }
}
