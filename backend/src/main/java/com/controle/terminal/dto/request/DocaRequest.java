package com.controle.terminal.dto.request;

import com.controle.terminal.domain.enums.StatusDoca;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DocaRequest(
        @NotNull(message = "O terminal e obrigatorio.")
        Long terminalId,

        @NotBlank(message = "O codigo da doca e obrigatorio.")
        @Size(max = 20, message = "O codigo deve ter no maximo 20 caracteres.")
        String codigo,

        @Size(max = 150, message = "A localizacao deve ter no maximo 150 caracteres.")
        String localizacao,

        StatusDoca status
) {
}
