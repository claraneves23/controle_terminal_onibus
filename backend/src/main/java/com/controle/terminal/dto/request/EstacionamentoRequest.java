package com.controle.terminal.dto.request;

import com.controle.terminal.domain.enums.StatusEstacionamento;
import jakarta.validation.constraints.*;

public record EstacionamentoRequest(
        @NotNull(message = "O terminal e obrigatorio.")
        Long terminalId,

        @NotBlank(message = "O nome do estacionamento e obrigatorio.")
        @Size(max = 100, message = "O nome deve ter no maximo 100 caracteres.")
        String nome,

        @NotNull(message = "A capacidade e obrigatoria.")
        @Positive(message = "A capacidade deve ser maior que zero.")
        Integer capacidade,

        StatusEstacionamento status
) {
}
