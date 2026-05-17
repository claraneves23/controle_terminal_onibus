package com.controle.terminal.dto.request;

import com.controle.terminal.domain.enums.PerfilUsuario;
import jakarta.validation.constraints.*;

public record UsuarioRequest(
        @NotBlank(message = "O nome e obrigatorio.")
        @Size(max = 100)
        String nome,

        @NotBlank(message = "O email e obrigatorio.")
        @Email(message = "O email informado e invalido.")
        @Size(max = 120)
        String email,

        @NotNull(message = "O perfil e obrigatorio.")
        PerfilUsuario perfil,

        Boolean ativo,

        @Size(min = 4, max = 100, message = "A senha deve ter entre 4 e 100 caracteres.")
        String senha
) {
}
