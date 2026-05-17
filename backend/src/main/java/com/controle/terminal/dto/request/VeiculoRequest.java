package com.controle.terminal.dto.request;

import com.controle.terminal.domain.enums.TipoEmpresa;
import com.controle.terminal.domain.enums.TipoVeiculo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VeiculoRequest(
        @NotBlank(message = "A placa e obrigatoria.")
        @Pattern(
                regexp = "^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$",
                message = "A placa deve estar no formato Mercosul (ex.: ABC1D23) ou antigo (ex.: ABC1234)."
        )
        @Size(max = 10, message = "A placa deve ter no maximo 10 caracteres.")
        String placa,

        @NotNull(message = "O tipo de veiculo e obrigatorio.")
        TipoVeiculo tipo,

        @NotBlank(message = "A empresa responsavel e obrigatoria.")
        @Size(max = 150)
        String empresaResponsavel,

        @NotNull(message = "O tipo da empresa e obrigatorio.")
        TipoEmpresa tipoEmpresa,

        @Size(max = 80)
        String modelo
) {
}
