package com.controle.terminal.dto.request;

import jakarta.validation.constraints.NotBlank;

public record EncerrarIncidenteRequest(
        @NotBlank(message = "A acao tomada e obrigatoria ao encerrar o incidente.")
        String acaoTomada
) {
}
