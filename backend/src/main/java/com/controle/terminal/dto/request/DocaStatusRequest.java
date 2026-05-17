package com.controle.terminal.dto.request;

import com.controle.terminal.domain.enums.StatusDoca;
import jakarta.validation.constraints.NotNull;

public record DocaStatusRequest(
        @NotNull(message = "O status e obrigatorio.")
        StatusDoca status
) {
}
