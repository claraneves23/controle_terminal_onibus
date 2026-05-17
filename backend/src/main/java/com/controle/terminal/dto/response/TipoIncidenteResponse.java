package com.controle.terminal.dto.response;

import com.controle.terminal.domain.entity.TipoIncidente;
import com.controle.terminal.domain.enums.NivelGravidade;

public record TipoIncidenteResponse(
        Long id,
        String nome,
        String descricao,
        NivelGravidade nivelGravidade
) {
    public static TipoIncidenteResponse from(TipoIncidente t) {
        return new TipoIncidenteResponse(t.getId(), t.getNome(), t.getDescricao(), t.getNivelGravidade());
    }
}
