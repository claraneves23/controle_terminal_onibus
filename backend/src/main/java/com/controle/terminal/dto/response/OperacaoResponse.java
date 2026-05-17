package com.controle.terminal.dto.response;

import com.controle.terminal.domain.entity.OperacaoCarga;
import com.controle.terminal.domain.enums.StatusOperacao;
import com.controle.terminal.domain.enums.TipoOperacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OperacaoResponse(
        Long id,
        Long terminalId,
        String terminalNome,
        Long docaId,
        String docaCodigo,
        Long vagaId,
        String vagaCodigo,
        Long veiculoId,
        String veiculoPlaca,
        Long usuarioId,
        String usuarioNome,
        TipoOperacao tipo,
        StatusOperacao status,
        String descricaoCarga,
        Integer quantidadeVolume,
        BigDecimal pesoEstimado,
        LocalDateTime agendadaEm,
        LocalDateTime iniciadaEm,
        LocalDateTime finalizadaEm,
        String observacao,
        List<DocumentoCargaResponse> documentos
) {
    public static OperacaoResponse from(OperacaoCarga o, List<DocumentoCargaResponse> documentos) {
        return new OperacaoResponse(
                o.getId(),
                o.getTerminal() != null ? o.getTerminal().getId() : null,
                o.getTerminal() != null ? o.getTerminal().getNome() : null,
                o.getDoca() != null ? o.getDoca().getId() : null,
                o.getDoca() != null ? o.getDoca().getCodigo() : null,
                o.getVaga() != null ? o.getVaga().getId() : null,
                o.getVaga() != null ? o.getVaga().getCodigo() : null,
                o.getVeiculo() != null ? o.getVeiculo().getId() : null,
                o.getVeiculo() != null ? o.getVeiculo().getPlaca() : null,
                o.getUsuario() != null ? o.getUsuario().getId() : null,
                o.getUsuario() != null ? o.getUsuario().getNome() : null,
                o.getTipo(),
                o.getStatus(),
                o.getDescricaoCarga(),
                o.getQuantidadeVolume(),
                o.getPesoEstimado(),
                o.getAgendadaEm(),
                o.getIniciadaEm(),
                o.getFinalizadaEm(),
                o.getObservacao(),
                documentos
        );
    }
}
