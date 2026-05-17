package com.controle.terminal.dto.request;

import com.controle.terminal.domain.enums.StatusVaga;
import jakarta.validation.constraints.NotNull;

public record VagaStatusRequest(
        @NotNull(message = "O status e obrigatorio.")
        StatusVaga status
) {
}
