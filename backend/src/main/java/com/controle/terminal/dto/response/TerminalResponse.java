package com.controle.terminal.dto.response;

import com.controle.terminal.domain.entity.Terminal;

public record TerminalResponse(
        Long id,
        String nome,
        String endereco,
        String cidade,
        Boolean ativo
) {
    public static TerminalResponse from(Terminal t) {
        return new TerminalResponse(t.getId(), t.getNome(), t.getEndereco(), t.getCidade(), t.getAtivo());
    }
}
