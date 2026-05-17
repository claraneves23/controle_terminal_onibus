package com.controle.terminal.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "O email e obrigatorio.")
        @Email(message = "O email informado e invalido.")
        String email,

        @NotBlank(message = "A senha e obrigatoria.")
        @Size(min = 4, max = 100, message = "A senha deve ter entre 4 e 100 caracteres.")
        String senha
) {
}
