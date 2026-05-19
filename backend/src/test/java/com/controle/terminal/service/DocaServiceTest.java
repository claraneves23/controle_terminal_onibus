package com.controle.terminal.service;

import com.controle.terminal.domain.entity.Doca;
import com.controle.terminal.domain.entity.Terminal;
import com.controle.terminal.domain.enums.StatusDoca;
import com.controle.terminal.dto.request.DocaRequest;
import com.controle.terminal.dto.request.DocaStatusRequest;
import com.controle.terminal.dto.response.DocaResponse;
import com.controle.terminal.exception.DuplicateResourceException;
import com.controle.terminal.exception.ResourceNotFoundException;
import com.controle.terminal.repository.DocaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocaServiceTest {

    @Mock
    private DocaRepository docaRepository;

    @Mock
    private TerminalService terminalService;

    @InjectMocks
    private DocaService docaService;

    private Terminal terminal;
    private Doca doca;

    @BeforeEach
    void setUp() {
        terminal = Terminal.builder().id(1L).nome("T1").cidade("SP").ativo(true).build();
        doca = Doca.builder()
                .id(10L)
                .terminal(terminal)
                .codigo("D-01")
                .status(StatusDoca.DISPONIVEL)
                .build();
    }

    @Test
    void create_deveDefinirStatusDisponivelQuandoNaoInformado() {
        DocaRequest request = new DocaRequest(1L, "D-02", "Setor A", null);
        when(terminalService.loadEntity(1L)).thenReturn(terminal);
        when(docaRepository.existsByTerminalIdAndCodigoIgnoreCase(1L, "D-02")).thenReturn(false);
        when(docaRepository.save(any(Doca.class))).thenAnswer(inv -> {
            Doca d = inv.getArgument(0);
            d.setId(11L);
            return d;
        });

        DocaResponse response = docaService.create(request);

        assertThat(response.status()).isEqualTo(StatusDoca.DISPONIVEL);
        assertThat(response.codigo()).isEqualTo("D-02");
    }

    @Test
    void create_deveLancarDuplicateResourceQuandoCodigoJaExisteNoTerminal() {
        DocaRequest request = new DocaRequest(1L, "D-01", null, StatusDoca.DISPONIVEL);
        when(terminalService.loadEntity(1L)).thenReturn(terminal);
        when(docaRepository.existsByTerminalIdAndCodigoIgnoreCase(1L, "D-01")).thenReturn(true);

        assertThatThrownBy(() -> docaService.create(request))
                .isInstanceOf(DuplicateResourceException.class);
        verify(docaRepository, never()).save(any());
    }

    @Test
    void updateStatus_deveAtualizarStatusDaDoca() {
        when(docaRepository.findById(10L)).thenReturn(Optional.of(doca));

        DocaResponse response = docaService.updateStatus(10L, new DocaStatusRequest(StatusDoca.MANUTENCAO));

        assertThat(response.status()).isEqualTo(StatusDoca.MANUTENCAO);
        assertThat(doca.getStatus()).isEqualTo(StatusDoca.MANUTENCAO);
    }

    @Test
    void update_naoDeveChecarDuplicidadeQuandoCodigoETerminalNaoMudaram() {
        DocaRequest request = new DocaRequest(1L, "D-01", "Nova localizacao", StatusDoca.DISPONIVEL);
        when(docaRepository.findById(10L)).thenReturn(Optional.of(doca));
        when(terminalService.loadEntity(1L)).thenReturn(terminal);

        DocaResponse response = docaService.update(10L, request);

        assertThat(response.localizacao()).isEqualTo("Nova localizacao");
        verify(docaRepository, never()).existsByTerminalIdAndCodigoIgnoreCase(any(), any());
    }

    @Test
    void loadEntity_deveLancarResourceNotFoundQuandoNaoExistir() {
        when(docaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> docaService.loadEntity(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
