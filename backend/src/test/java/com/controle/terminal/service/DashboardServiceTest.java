package com.controle.terminal.service;

import com.controle.terminal.domain.entity.Doca;
import com.controle.terminal.domain.entity.Terminal;
import com.controle.terminal.domain.enums.NivelGravidade;
import com.controle.terminal.domain.enums.StatusDoca;
import com.controle.terminal.domain.enums.StatusIncidente;
import com.controle.terminal.domain.enums.StatusOperacao;
import com.controle.terminal.dto.response.DashboardResponse;
import com.controle.terminal.repository.DocaRepository;
import com.controle.terminal.repository.IncidenteRepository;
import com.controle.terminal.repository.OperacaoCargaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.Assertions.offset;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private DocaRepository docaRepository;

    @Mock
    private OperacaoCargaRepository operacaoRepository;

    @Mock
    private IncidenteRepository incidenteRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void resumo_semFiltroDeTerminal_deveConsolidarMetricasGlobais() {
        when(docaRepository.count()).thenReturn(10L);
        when(docaRepository.countByStatus(StatusDoca.OCUPADA)).thenReturn(4L);
        when(operacaoRepository.countByStatus(StatusOperacao.AGENDADA)).thenReturn(2L);
        when(operacaoRepository.countByStatus(StatusOperacao.EM_ANDAMENTO)).thenReturn(3L);
        when(operacaoRepository.countFinalizadasDesde(any(LocalDateTime.class), isNull())).thenReturn(5L);
        when(operacaoRepository.tempoMedioSegundosDesde(any(LocalDateTime.class), isNull())).thenReturn(1800.0);
        when(incidenteRepository.countByStatus(StatusIncidente.ABERTO)).thenReturn(7L);
        when(incidenteRepository.contagemAbertosPorGravidade(isNull())).thenReturn(Collections.emptyList());
        when(operacaoRepository.findRecentByStatuses(any(), any())).thenReturn(Collections.emptyList());
        when(docaRepository.findAll()).thenReturn(Collections.emptyList());

        DashboardResponse response = dashboardService.resumo(null);

        assertThat(response.totalDocas()).isEqualTo(10L);
        assertThat(response.docasOcupadas()).isEqualTo(4L);
        assertThat(response.taxaOcupacaoDocas()).isEqualTo(0.4, offset(0.001));
        assertThat(response.operacoesAgendadas()).isEqualTo(2L);
        assertThat(response.operacoesEmAndamento()).isEqualTo(3L);
        assertThat(response.operacoesFinalizadasHoje()).isEqualTo(5L);
        assertThat(response.veiculosNoPatio()).isEqualTo(5L); // agendadas + andamento
        assertThat(response.tempoMedioSegundosHoje()).isEqualTo(1800.0);
        assertThat(response.incidentesAbertos()).isEqualTo(7L);
        // Mapa por gravidade sempre tem todos os niveis (com zero quando ausentes)
        for (NivelGravidade n : NivelGravidade.values()) {
            assertThat(response.incidentesAbertosPorGravidade()).containsKey(n);
        }
    }

    @Test
    void resumo_comTerminalFiltrado_deveUsarConsultasPorTerminal() {
        Terminal terminal = Terminal.builder().id(7L).nome("T").cidade("SP").ativo(true).build();
        Doca doca = Doca.builder().id(1L).codigo("D-01").terminal(terminal).status(StatusDoca.DISPONIVEL).build();

        when(docaRepository.findByTerminalIdOrderByCodigo(7L)).thenReturn(List.of(doca));
        when(docaRepository.countByTerminalIdAndStatus(7L, StatusDoca.OCUPADA)).thenReturn(0L);
        when(operacaoRepository.countByTerminalIdAndStatus(7L, StatusOperacao.AGENDADA)).thenReturn(1L);
        when(operacaoRepository.countByTerminalIdAndStatus(7L, StatusOperacao.EM_ANDAMENTO)).thenReturn(0L);
        when(operacaoRepository.countFinalizadasDesde(any(LocalDateTime.class), eq(7L))).thenReturn(0L);
        when(operacaoRepository.tempoMedioSegundosDesde(any(LocalDateTime.class), eq(7L))).thenReturn(null);
        when(incidenteRepository.countByStatus(StatusIncidente.ABERTO)).thenReturn(0L);
        when(incidenteRepository.contagemAbertosPorGravidade(7L)).thenReturn(Collections.emptyList());
        when(operacaoRepository.findRecentByStatuses(any(), any())).thenReturn(Collections.emptyList());

        DashboardResponse response = dashboardService.resumo(7L);

        assertThat(response.totalDocas()).isEqualTo(1L);
        assertThat(response.docasOcupadas()).isEqualTo(0L);
        assertThat(response.taxaOcupacaoDocas()).isEqualTo(0.0, within(0.001));
        assertThat(response.docas()).hasSize(1);
        assertThat(response.tempoMedioSegundosHoje()).isNull();
    }

    @Test
    void resumo_quandoNenhumaDocaCadastrada_deveRetornarTaxaZero() {
        when(docaRepository.count()).thenReturn(0L);
        when(docaRepository.countByStatus(StatusDoca.OCUPADA)).thenReturn(0L);
        when(operacaoRepository.countByStatus(any())).thenReturn(0L);
        when(operacaoRepository.countFinalizadasDesde(any(), isNull())).thenReturn(0L);
        when(operacaoRepository.tempoMedioSegundosDesde(any(), isNull())).thenReturn(null);
        when(incidenteRepository.countByStatus(StatusIncidente.ABERTO)).thenReturn(0L);
        when(incidenteRepository.contagemAbertosPorGravidade(isNull())).thenReturn(Collections.emptyList());
        when(operacaoRepository.findRecentByStatuses(any(), any())).thenReturn(Collections.emptyList());
        when(docaRepository.findAll()).thenReturn(Collections.emptyList());

        DashboardResponse response = dashboardService.resumo(null);

        assertThat(response.taxaOcupacaoDocas()).isEqualTo(0.0);
    }
}
