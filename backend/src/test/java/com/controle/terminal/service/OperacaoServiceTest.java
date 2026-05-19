package com.controle.terminal.service;

import com.controle.terminal.domain.entity.Doca;
import com.controle.terminal.domain.entity.OperacaoCarga;
import com.controle.terminal.domain.entity.Terminal;
import com.controle.terminal.domain.entity.VagaEstacionamento;
import com.controle.terminal.domain.entity.Veiculo;
import com.controle.terminal.domain.enums.StatusDoca;
import com.controle.terminal.domain.enums.StatusOperacao;
import com.controle.terminal.domain.enums.StatusVaga;
import com.controle.terminal.domain.enums.TipoOperacao;
import com.controle.terminal.dto.request.CheckinRequest;
import com.controle.terminal.dto.response.OperacaoResponse;
import com.controle.terminal.exception.BusinessException;
import com.controle.terminal.exception.DocaUnavailableException;
import com.controle.terminal.exception.InvalidStateTransitionException;
import com.controle.terminal.exception.OperacaoNotFinishedException;
import com.controle.terminal.exception.VagaUnavailableException;
import com.controle.terminal.repository.DocumentoCargaRepository;
import com.controle.terminal.repository.OperacaoCargaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperacaoServiceTest {

    @Mock
    private OperacaoCargaRepository operacaoRepository;

    @Mock
    private DocumentoCargaRepository documentoRepository;

    @Mock
    private TerminalService terminalService;

    @Mock
    private DocaService docaService;

    @Mock
    private VagaService vagaService;

    @Mock
    private VeiculoService veiculoService;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private OperacaoService operacaoService;

    private Terminal terminal;
    private Doca docaDisponivel;
    private VagaEstacionamento vagaLivre;
    private Veiculo veiculo;

    @BeforeEach
    void setUp() {
        terminal = Terminal.builder().id(1L).nome("T1").cidade("SP").ativo(true).build();
        docaDisponivel = Doca.builder().id(10L).terminal(terminal).codigo("D-01").status(StatusDoca.DISPONIVEL).build();
        vagaLivre = VagaEstacionamento.builder().id(20L).codigo("V-01").status(StatusVaga.LIVRE).build();
        veiculo = Veiculo.builder().id(30L).placa("ABC1D23").build();
    }

    @Test
    void checkin_deveLancarBusinessQuandoDocaEVagaInformadasJuntas() {
        CheckinRequest request = new CheckinRequest(1L, 30L, TipoOperacao.CARGA, 10L, 20L,
                null, null, null, null);

        assertThatThrownBy(() -> operacaoService.checkin(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("doca ou vaga");
    }

    @Test
    void checkin_comDocaDisponivel_deveMarcarOcupadaECriarOperacaoAgendada() {
        CheckinRequest request = new CheckinRequest(1L, 30L, TipoOperacao.CARGA, 10L, null,
                "Carga A", 10, null, null);
        when(terminalService.loadEntity(1L)).thenReturn(terminal);
        when(veiculoService.loadEntity(30L)).thenReturn(veiculo);
        when(docaService.loadEntity(10L)).thenReturn(docaDisponivel);
        when(operacaoRepository.save(any(OperacaoCarga.class))).thenAnswer(inv -> {
            OperacaoCarga op = inv.getArgument(0);
            op.setId(100L);
            return op;
        });

        OperacaoResponse response = operacaoService.checkin(request);

        assertThat(response.status()).isEqualTo(StatusOperacao.AGENDADA);
        assertThat(docaDisponivel.getStatus()).isEqualTo(StatusDoca.OCUPADA);
        assertThat(response.agendadaEm()).isNotNull();
    }

    @Test
    void checkin_deveLancarDocaUnavailableQuandoDocaNaoEstaDisponivel() {
        docaDisponivel.setStatus(StatusDoca.OCUPADA);
        CheckinRequest request = new CheckinRequest(1L, 30L, TipoOperacao.CARGA, 10L, null,
                null, null, null, null);
        when(terminalService.loadEntity(1L)).thenReturn(terminal);
        when(veiculoService.loadEntity(30L)).thenReturn(veiculo);
        when(docaService.loadEntity(10L)).thenReturn(docaDisponivel);

        assertThatThrownBy(() -> operacaoService.checkin(request))
                .isInstanceOf(DocaUnavailableException.class);
    }

    @Test
    void checkin_comVagaLivre_deveMarcarVagaOcupada() {
        CheckinRequest request = new CheckinRequest(1L, 30L, TipoOperacao.DESCARGA, null, 20L,
                null, null, null, null);
        when(terminalService.loadEntity(1L)).thenReturn(terminal);
        when(veiculoService.loadEntity(30L)).thenReturn(veiculo);
        when(vagaService.loadEntity(20L)).thenReturn(vagaLivre);
        when(operacaoRepository.save(any(OperacaoCarga.class))).thenAnswer(inv -> inv.getArgument(0));

        operacaoService.checkin(request);

        assertThat(vagaLivre.getStatus()).isEqualTo(StatusVaga.OCUPADA);
    }

    @Test
    void checkin_deveLancarVagaUnavailableQuandoVagaNaoEstaLivre() {
        vagaLivre.setStatus(StatusVaga.RESERVADA);
        CheckinRequest request = new CheckinRequest(1L, 30L, TipoOperacao.DESCARGA, null, 20L,
                null, null, null, null);
        when(terminalService.loadEntity(1L)).thenReturn(terminal);
        when(veiculoService.loadEntity(30L)).thenReturn(veiculo);
        when(vagaService.loadEntity(20L)).thenReturn(vagaLivre);

        assertThatThrownBy(() -> operacaoService.checkin(request))
                .isInstanceOf(VagaUnavailableException.class);
    }

    @Test
    void iniciar_deveTransicionarAgendadaParaEmAndamento() {
        OperacaoCarga op = OperacaoCarga.builder()
                .id(100L).terminal(terminal).veiculo(veiculo)
                .doca(docaDisponivel)
                .status(StatusOperacao.AGENDADA)
                .build();
        when(operacaoRepository.findById(100L)).thenReturn(Optional.of(op));
        when(documentoRepository.findByOperacaoIdOrderById(100L)).thenReturn(Collections.emptyList());

        OperacaoResponse response = operacaoService.iniciar(100L);

        assertThat(response.status()).isEqualTo(StatusOperacao.EM_ANDAMENTO);
        assertThat(op.getIniciadaEm()).isNotNull();
    }

    @Test
    void iniciar_deveLancarInvalidStateTransitionSeNaoEstiverAgendada() {
        OperacaoCarga op = OperacaoCarga.builder()
                .id(100L).status(StatusOperacao.EM_ANDAMENTO).doca(docaDisponivel).build();
        when(operacaoRepository.findById(100L)).thenReturn(Optional.of(op));

        assertThatThrownBy(() -> operacaoService.iniciar(100L))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void iniciar_deveLancarBusinessQuandoNaoHaDocaNemVaga() {
        OperacaoCarga op = OperacaoCarga.builder()
                .id(100L).status(StatusOperacao.AGENDADA).build();
        when(operacaoRepository.findById(100L)).thenReturn(Optional.of(op));

        assertThatThrownBy(() -> operacaoService.iniciar(100L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("doca ou vaga");
    }

    @Test
    void finalizar_deveTransicionarEmAndamentoParaFinalizada() {
        OperacaoCarga op = OperacaoCarga.builder()
                .id(100L).status(StatusOperacao.EM_ANDAMENTO).doca(docaDisponivel).build();
        docaDisponivel.setStatus(StatusDoca.OCUPADA);
        when(operacaoRepository.findById(100L)).thenReturn(Optional.of(op));
        when(documentoRepository.findByOperacaoIdOrderById(100L)).thenReturn(Collections.emptyList());

        OperacaoResponse response = operacaoService.finalizar(100L);

        assertThat(response.status()).isEqualTo(StatusOperacao.FINALIZADA);
        assertThat(op.getFinalizadaEm()).isNotNull();
        // Doca segue OCUPADA — so e liberada no checkout
        assertThat(docaDisponivel.getStatus()).isEqualTo(StatusDoca.OCUPADA);
    }

    @Test
    void finalizar_deveLancarInvalidStateTransitionSeNaoEstaEmAndamento() {
        OperacaoCarga op = OperacaoCarga.builder().id(100L).status(StatusOperacao.AGENDADA).build();
        when(operacaoRepository.findById(100L)).thenReturn(Optional.of(op));

        assertThatThrownBy(() -> operacaoService.finalizar(100L))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void checkout_deveLiberarDocaOcupada() {
        docaDisponivel.setStatus(StatusDoca.OCUPADA);
        OperacaoCarga op = OperacaoCarga.builder()
                .id(100L).status(StatusOperacao.FINALIZADA).doca(docaDisponivel).build();
        when(operacaoRepository.findById(100L)).thenReturn(Optional.of(op));
        when(documentoRepository.findByOperacaoIdOrderById(100L)).thenReturn(Collections.emptyList());

        operacaoService.checkout(100L);

        assertThat(docaDisponivel.getStatus()).isEqualTo(StatusDoca.DISPONIVEL);
    }

    @Test
    void checkout_deveLiberarVagaOcupada() {
        vagaLivre.setStatus(StatusVaga.OCUPADA);
        OperacaoCarga op = OperacaoCarga.builder()
                .id(100L).status(StatusOperacao.FINALIZADA).vaga(vagaLivre).build();
        when(operacaoRepository.findById(100L)).thenReturn(Optional.of(op));
        when(documentoRepository.findByOperacaoIdOrderById(100L)).thenReturn(Collections.emptyList());

        operacaoService.checkout(100L);

        assertThat(vagaLivre.getStatus()).isEqualTo(StatusVaga.LIVRE);
    }

    @Test
    void checkout_deveLancarOperacaoNotFinishedSeNaoFinalizada() {
        OperacaoCarga op = OperacaoCarga.builder().id(100L).status(StatusOperacao.EM_ANDAMENTO).build();
        when(operacaoRepository.findById(100L)).thenReturn(Optional.of(op));

        assertThatThrownBy(() -> operacaoService.checkout(100L))
                .isInstanceOf(OperacaoNotFinishedException.class);
    }

    @Test
    void checkout_deveSerIdempotenteQuandoDocaJaLiberada() {
        // Doca ja esta DISPONIVEL — checkout nao deve quebrar
        OperacaoCarga op = OperacaoCarga.builder()
                .id(100L).status(StatusOperacao.FINALIZADA).doca(docaDisponivel).build();
        when(operacaoRepository.findById(100L)).thenReturn(Optional.of(op));
        when(documentoRepository.findByOperacaoIdOrderById(100L)).thenReturn(Collections.emptyList());

        operacaoService.checkout(100L);

        assertThat(docaDisponivel.getStatus()).isEqualTo(StatusDoca.DISPONIVEL);
    }

    @Test
    void cancelar_deveBloquearOperacaoFinalizada() {
        OperacaoCarga op = OperacaoCarga.builder().id(100L).status(StatusOperacao.FINALIZADA).build();
        when(operacaoRepository.findById(100L)).thenReturn(Optional.of(op));

        assertThatThrownBy(() -> operacaoService.cancelar(100L))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void cancelar_deveLiberarRecursosEAtualizarStatus() {
        docaDisponivel.setStatus(StatusDoca.OCUPADA);
        OperacaoCarga op = OperacaoCarga.builder()
                .id(100L).status(StatusOperacao.AGENDADA).doca(docaDisponivel).build();
        when(operacaoRepository.findById(100L)).thenReturn(Optional.of(op));
        when(documentoRepository.findByOperacaoIdOrderById(100L)).thenReturn(Collections.emptyList());

        OperacaoResponse response = operacaoService.cancelar(100L);

        assertThat(response.status()).isEqualTo(StatusOperacao.CANCELADA);
        assertThat(docaDisponivel.getStatus()).isEqualTo(StatusDoca.DISPONIVEL);
    }

    @Test
    void cancelar_jaCancelada_devePermanecerCancelada() {
        OperacaoCarga op = OperacaoCarga.builder().id(100L).status(StatusOperacao.CANCELADA).build();
        when(operacaoRepository.findById(100L)).thenReturn(Optional.of(op));
        when(documentoRepository.findByOperacaoIdOrderById(100L)).thenReturn(Collections.emptyList());

        OperacaoResponse response = operacaoService.cancelar(100L);

        assertThat(response.status()).isEqualTo(StatusOperacao.CANCELADA);
    }
}
