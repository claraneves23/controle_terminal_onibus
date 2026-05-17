package com.controle.terminal.dto.response;

import com.controle.terminal.domain.entity.Incidente;
import com.controle.terminal.domain.enums.NivelGravidade;
import com.controle.terminal.domain.enums.StatusIncidente;

import java.time.LocalDateTime;

public record IncidenteResponse(
        Long id,
        Long tipoIncidenteId,
        String tipoIncidenteNome,
        NivelGravidade nivelGravidade,
        Long terminalId,
        String terminalNome,
        Long docaId,
        String docaCodigo,
        Long estacionamentoId,
        Long vagaId,
        String vagaCodigo,
        Long operacaoId,
        Long usuarioRegistroId,
        String usuarioRegistroNome,
        LocalDateTime ocorridoEm,
        String descricao,
        StatusIncidente status,
        String acaoTomada,
        LocalDateTime encerradoEm
) {
    public static IncidenteResponse from(Incidente i) {
        return new IncidenteResponse(
                i.getId(),
                i.getTipo() != null ? i.getTipo().getId() : null,
                i.getTipo() != null ? i.getTipo().getNome() : null,
                i.getTipo() != null ? i.getTipo().getNivelGravidade() : null,
                i.getTerminal() != null ? i.getTerminal().getId() : null,
                i.getTerminal() != null ? i.getTerminal().getNome() : null,
                i.getDoca() != null ? i.getDoca().getId() : null,
                i.getDoca() != null ? i.getDoca().getCodigo() : null,
                i.getEstacionamento() != null ? i.getEstacionamento().getId() : null,
                i.getVaga() != null ? i.getVaga().getId() : null,
                i.getVaga() != null ? i.getVaga().getCodigo() : null,
                i.getOperacao() != null ? i.getOperacao().getId() : null,
                i.getUsuarioRegistro() != null ? i.getUsuarioRegistro().getId() : null,
                i.getUsuarioRegistro() != null ? i.getUsuarioRegistro().getNome() : null,
                i.getOcorridoEm(),
                i.getDescricao(),
                i.getStatus(),
                i.getAcaoTomada(),
                i.getEncerradoEm()
        );
    }
}
