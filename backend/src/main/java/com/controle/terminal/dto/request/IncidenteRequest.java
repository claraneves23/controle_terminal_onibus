package com.controle.terminal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record IncidenteRequest(
        @NotNull(message = "O tipo de incidente e obrigatorio.")
        Long tipoIncidenteId,

        @NotNull(message = "O terminal e obrigatorio.")
        Long terminalId,

        Long docaId,
        Long estacionamentoId,
        Long vagaId,
        Long operacaoId,

        @NotNull(message = "A data do incidente e obrigatoria.")
        LocalDateTime ocorridoEm,

        @NotBlank(message = "A descricao do incidente e obrigatoria.")
        String descricao,

        String acaoTomada
) {
}
