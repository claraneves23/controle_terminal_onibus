package com.controle.terminal.dto.response;

import com.controle.terminal.domain.entity.Usuario;
import com.controle.terminal.domain.enums.PerfilUsuario;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        PerfilUsuario perfil,
        Boolean ativo,
        LocalDateTime ultimoLogin,
        LocalDateTime criadoEm
) {
    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(
                u.getId(),
                u.getNome(),
                u.getEmail(),
                u.getPerfil(),
                u.getAtivo(),
                u.getUltimoLogin(),
                u.getCriadoEm()
        );
    }
}
