package com.controle.terminal.dto.request;

import com.controle.terminal.domain.enums.TipoDocumento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record DocumentoCargaRequest(
        @NotNull(message = "O tipo do documento e obrigatorio.")
        TipoDocumento tipo,

        @NotBlank(message = "O numero do documento e obrigatorio.")
        @Size(max = 80)
        String numero,

        LocalDate emitidoEm,

        String observacao
) {
}
