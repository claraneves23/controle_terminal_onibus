package com.controle.terminal.dto.response;

import com.controle.terminal.domain.entity.DocumentoCarga;
import com.controle.terminal.domain.enums.TipoDocumento;

import java.time.LocalDate;

public record DocumentoCargaResponse(
        Long id,
        Long operacaoId,
        TipoDocumento tipo,
        String numero,
        LocalDate emitidoEm,
        String observacao
) {
    public static DocumentoCargaResponse from(DocumentoCarga d) {
        return new DocumentoCargaResponse(
                d.getId(),
                d.getOperacao() != null ? d.getOperacao().getId() : null,
                d.getTipo(),
                d.getNumero(),
                d.getEmitidoEm(),
                d.getObservacao()
        );
    }
}
