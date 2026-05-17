package com.controle.terminal.dto.request;

import com.controle.terminal.domain.enums.StatusVaga;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VagaRequest(
        @NotNull(message = "O estacionamento e obrigatorio.")
        Long estacionamentoId,

        @NotBlank(message = "O codigo da vaga e obrigatorio.")
        @Size(max = 20, message = "O codigo deve ter no maximo 20 caracteres.")
        String codigo,

        StatusVaga status
) {
}
