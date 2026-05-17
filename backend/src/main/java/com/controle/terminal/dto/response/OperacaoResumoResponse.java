package com.controle.terminal.dto.response;

import com.controle.terminal.domain.entity.OperacaoCarga;
import com.controle.terminal.domain.enums.StatusOperacao;
import com.controle.terminal.domain.enums.TipoOperacao;

import java.time.LocalDateTime;

public record OperacaoResumoResponse(
        Long id,
        Long terminalId,
        String terminalNome,
        String docaCodigo,
        String vagaCodigo,
        String veiculoPlaca,
        TipoOperacao tipo,
        StatusOperacao status,
        LocalDateTime agendadaEm,
        LocalDateTime iniciadaEm,
        LocalDateTime finalizadaEm
) {
    public static OperacaoResumoResponse from(OperacaoCarga o) {
        return new OperacaoResumoResponse(
                o.getId(),
                o.getTerminal() != null ? o.getTerminal().getId() : null,
                o.getTerminal() != null ? o.getTerminal().getNome() : null,
                o.getDoca() != null ? o.getDoca().getCodigo() : null,
                o.getVaga() != null ? o.getVaga().getCodigo() : null,
                o.getVeiculo() != null ? o.getVeiculo().getPlaca() : null,
                o.getTipo(),
                o.getStatus(),
                o.getAgendadaEm(),
                o.getIniciadaEm(),
                o.getFinalizadaEm()
        );
    }
}
