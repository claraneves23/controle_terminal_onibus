package com.controle.terminal.dto.request;

import com.controle.terminal.domain.enums.TipoOperacao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CheckinRequest(
        @NotNull(message = "O terminal e obrigatorio.")
        Long terminalId,

        @NotNull(message = "O veiculo e obrigatorio.")
        Long veiculoId,

        @NotNull(message = "O tipo de operacao (CARGA/DESCARGA) e obrigatorio.")
        TipoOperacao tipo,

        Long docaId,

        Long vagaId,

        @Size(max = 200)
        String descricaoCarga,

        @PositiveOrZero(message = "A quantidade de volume deve ser zero ou maior.")
        Integer quantidadeVolume,

        @PositiveOrZero(message = "O peso estimado deve ser zero ou maior.")
        BigDecimal pesoEstimado,

        String observacao
) {
}
