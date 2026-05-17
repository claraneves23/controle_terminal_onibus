package com.controle.terminal.dto.response;

import com.controle.terminal.domain.entity.Doca;
import com.controle.terminal.domain.enums.StatusDoca;

public record DocaResponse(
        Long id,
        Long terminalId,
        String terminalNome,
        String codigo,
        String localizacao,
        StatusDoca status
) {
    public static DocaResponse from(Doca d) {
        return new DocaResponse(
                d.getId(),
                d.getTerminal() != null ? d.getTerminal().getId() : null,
                d.getTerminal() != null ? d.getTerminal().getNome() : null,
                d.getCodigo(),
                d.getLocalizacao(),
                d.getStatus()
        );
    }
}
