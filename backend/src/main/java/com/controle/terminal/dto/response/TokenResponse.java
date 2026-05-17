package com.controle.terminal.dto.response;

import com.controle.terminal.domain.enums.PerfilUsuario;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresInMs,
        UsuarioLogado usuario
) {
    public record UsuarioLogado(Long id, String nome, String email, PerfilUsuario perfil) {
    }
}
