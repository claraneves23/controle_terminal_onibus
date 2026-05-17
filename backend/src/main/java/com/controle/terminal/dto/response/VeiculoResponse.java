package com.controle.terminal.dto.response;

import com.controle.terminal.domain.entity.Veiculo;
import com.controle.terminal.domain.enums.TipoEmpresa;
import com.controle.terminal.domain.enums.TipoVeiculo;

public record VeiculoResponse(
        Long id,
        String placa,
        TipoVeiculo tipo,
        String empresaResponsavel,
        TipoEmpresa tipoEmpresa,
        String modelo
) {
    public static VeiculoResponse from(Veiculo v) {
        return new VeiculoResponse(
                v.getId(),
                v.getPlaca(),
                v.getTipo(),
                v.getEmpresaResponsavel(),
                v.getTipoEmpresa(),
                v.getModelo()
        );
    }
}
