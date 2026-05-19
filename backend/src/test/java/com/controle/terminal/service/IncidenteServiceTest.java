package com.controle.terminal.service;

import com.controle.terminal.domain.entity.Doca;
import com.controle.terminal.domain.entity.Incidente;
import com.controle.terminal.domain.entity.Terminal;
import com.controle.terminal.domain.entity.TipoIncidente;
import com.controle.terminal.domain.enums.StatusDoca;
import com.controle.terminal.domain.enums.NivelGravidade;
import com.controle.terminal.domain.enums.StatusIncidente;
import com.controle.terminal.dto.request.EncerrarIncidenteRequest;
import com.controle.terminal.dto.request.IncidenteRequest;
import com.controle.terminal.dto.response.IncidenteResponse;
import com.controle.terminal.exception.BusinessException;
import com.controle.terminal.exception.IncidentAlreadyClosedException;
import com.controle.terminal.exception.ResourceNotFoundException;
import com.controle.terminal.repository.IncidenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncidenteServiceTest {

    @Mock
    private IncidenteRepository incidenteRepository;

    @Mock
    private TipoIncidenteService tipoIncidenteService;

    @Mock
    private TerminalService terminalService;

    @Mock
    private DocaService docaService;

    @Mock
    private EstacionamentoService estacionamentoService;

    @Mock
    private VagaService vagaService;

    @Mock
    private OperacaoService operacaoService;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private IncidenteService incidenteService;

    private Terminal terminal;
    private TipoIncidente tipoIncidente;
    private Incidente incidenteAberto;

    @BeforeEach
    void setUp() {
        terminal = Terminal.builder().id(1L).nome("T1").cidade("SP").ativo(true).build();
        tipoIncidente = TipoIncidente.builder().id(2L).nome("Avaria").nivelGravidade(NivelGravidade.ALTO).build();
        incidenteAberto = Incidente.builder()
                .id(50L)
                .terminal(terminal)
                .tipo(tipoIncidente)
                .ocorridoEm(LocalDateTime.now().minusHours(1))
                .descricao("Carga avariada")
                .status(StatusIncidente.ABERTO)
                .build();
    }

    @Test
    void create_deveLancarBusinessQuandoDataDoIncidenteEhFutura() {
        IncidenteRequest request = new IncidenteRequest(2L, 1L, 10L, null, null, null,
                LocalDateTime.now().plusDays(1), "Algo", null);

        assertThatThrownBy(() -> incidenteService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("futuro");
    }

    @Test
    void create_deveLancarBusinessQuandoNenhumVinculoFoiInformado() {
        IncidenteRequest request = new IncidenteRequest(2L, 1L, null, null, null, null,
                LocalDateTime.now().minusMinutes(5), "Algo", null);

        assertThatThrownBy(() -> incidenteService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("vinculo");
    }

    @Test
    void create_deveSalvarIncidenteAbertoComVinculoDeDoca() {
        IncidenteRequest request = new IncidenteRequest(2L, 1L, 10L, null, null, null,
                LocalDateTime.now().minusMinutes(10), "Carga avariada", null);
        Doca doca = Doca.builder().id(10L).terminal(terminal).codigo("D-01").status(StatusDoca.DISPONIVEL).build();
        when(tipoIncidenteService.loadEntity(2L)).thenReturn(tipoIncidente);
        when(terminalService.loadEntity(1L)).thenReturn(terminal);
        when(docaService.loadEntity(10L)).thenReturn(doca);
        when(incidenteRepository.save(any(Incidente.class))).thenAnswer(inv -> {
            Incidente i = inv.getArgument(0);
            i.setId(99L);
            return i;
        });

        IncidenteResponse response = incidenteService.create(request);

        assertThat(response.status()).isEqualTo(StatusIncidente.ABERTO);
        assertThat(response.id()).isEqualTo(99L);
    }

    @Test
    void encerrar_deveDefinirResolvidoEEncerradoEm() {
        when(incidenteRepository.findById(50L)).thenReturn(Optional.of(incidenteAberto));

        IncidenteResponse response = incidenteService.encerrar(50L,
                new EncerrarIncidenteRequest("Reembalado e despachado"));

        assertThat(response.status()).isEqualTo(StatusIncidente.RESOLVIDO);
        assertThat(incidenteAberto.getEncerradoEm()).isNotNull();
        assertThat(incidenteAberto.getAcaoTomada()).isEqualTo("Reembalado e despachado");
    }

    @Test
    void encerrar_deveLancarIncidentAlreadyClosedQuandoJaResolvido() {
        incidenteAberto.setStatus(StatusIncidente.RESOLVIDO);
        when(incidenteRepository.findById(50L)).thenReturn(Optional.of(incidenteAberto));

        assertThatThrownBy(() -> incidenteService.encerrar(50L, new EncerrarIncidenteRequest("...")))
                .isInstanceOf(IncidentAlreadyClosedException.class);
    }

    @Test
    void update_deveLancarIncidentAlreadyClosedQuandoCancelado() {
        incidenteAberto.setStatus(StatusIncidente.CANCELADO);
        when(incidenteRepository.findById(50L)).thenReturn(Optional.of(incidenteAberto));

        IncidenteRequest request = new IncidenteRequest(2L, 1L, 10L, null, null, null,
                LocalDateTime.now(), "Desc", null);

        assertThatThrownBy(() -> incidenteService.update(50L, request))
                .isInstanceOf(IncidentAlreadyClosedException.class);
        verify(incidenteRepository, never()).save(any());
    }

    @Test
    void loadEntity_deveLancarResourceNotFoundQuandoNaoExistir() {
        when(incidenteRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incidenteService.loadEntity(404L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
