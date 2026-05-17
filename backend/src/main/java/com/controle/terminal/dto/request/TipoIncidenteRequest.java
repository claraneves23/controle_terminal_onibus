package com.controle.terminal.dto.request;

import com.controle.terminal.domain.enums.NivelGravidade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TipoIncidenteRequest(
        @NotBlank(message = "O nome do tipo de incidente e obrigatorio.")
        @Size(max = 100)
        String nome,

        String descricao,

        @NotNull(message = "O nivel de gravidade e obrigatorio.")
        NivelGravidade nivelGravidade
) {
}
