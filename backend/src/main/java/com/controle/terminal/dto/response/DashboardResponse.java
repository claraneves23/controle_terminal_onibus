package com.controle.terminal.dto.response;

import com.controle.terminal.domain.enums.NivelGravidade;

import java.util.List;
import java.util.Map;

public record DashboardResponse(
        long totalDocas,
        long docasOcupadas,
        double taxaOcupacaoDocas,

        long operacoesAgendadas,
        long operacoesEmAndamento,
        long operacoesFinalizadasHoje,

        long veiculosNoPatio,
        Double tempoMedioSegundosHoje,

        long incidentesAbertos,
        Map<NivelGravidade, Long> incidentesAbertosPorGravidade,

        List<OperacaoResumoResponse> operacoesRecentes,
        List<DocaResponse> docas
) {
}
