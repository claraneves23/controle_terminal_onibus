package com.controle.terminal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TerminalRequest(
        @NotBlank(message = "O nome do terminal e obrigatorio.")
        @Size(max = 100, message = "O nome deve ter no maximo 100 caracteres.")
        String nome,

        @Size(max = 200, message = "O endereco deve ter no maximo 200 caracteres.")
        String endereco,

        @NotBlank(message = "A cidade e obrigatoria.")
        @Size(max = 80, message = "A cidade deve ter no maximo 80 caracteres.")
        String cidade,

        Boolean ativo
) {
}
