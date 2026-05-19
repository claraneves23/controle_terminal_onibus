package com.controle.terminal.service;

import com.controle.terminal.domain.entity.Estacionamento;
import com.controle.terminal.domain.entity.Terminal;
import com.controle.terminal.domain.enums.StatusEstacionamento;
import com.controle.terminal.dto.request.EstacionamentoRequest;
import com.controle.terminal.dto.response.EstacionamentoResponse;
import com.controle.terminal.exception.ResourceNotFoundException;
import com.controle.terminal.repository.EstacionamentoRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstacionamentoServiceTest {

    @Mock
    private EstacionamentoRepository estacionamentoRepository;

    @Mock
    private TerminalService terminalService;

    @InjectMocks
    private EstacionamentoService estacionamentoService;

    private Terminal terminal;
    private Estacionamento estacionamento;

    @BeforeEach
    void setUp() {
        terminal = Terminal.builder().id(1L).nome("T1").cidade("SP").ativo(true).build();
        estacionamento = Estacionamento.builder()
                .id(5L)
                .terminal(terminal)
                .nome("Patio Norte")
                .capacidade(20)
                .status(StatusEstacionamento.ATIVO)
                .build();
    }

    @Test
    void create_deveDefinirStatusAtivoPadrao() {
        EstacionamentoRequest request = new EstacionamentoRequest(1L, "Patio Sul", 15, null);
        when(terminalService.loadEntity(1L)).thenReturn(terminal);
        when(estacionamentoRepository.save(any(Estacionamento.class))).thenAnswer(inv -> {
            Estacionamento e = inv.getArgument(0);
            e.setId(6L);
            return e;
        });

        EstacionamentoResponse response = estacionamentoService.create(request);

        assertThat(response.status()).isEqualTo(StatusEstacionamento.ATIVO);
        assertThat(response.capacidade()).isEqualTo(15);
    }

    @Test
    void update_deveAtualizarCamposBasicos() {
        EstacionamentoRequest request = new EstacionamentoRequest(1L, "Patio Norte Renomeado", 30, StatusEstacionamento.MANUTENCAO);
        when(estacionamentoRepository.findById(5L)).thenReturn(Optional.of(estacionamento));
        when(terminalService.loadEntity(1L)).thenReturn(terminal);

        EstacionamentoResponse response = estacionamentoService.update(5L, request);

        assertThat(response.nome()).isEqualTo("Patio Norte Renomeado");
        assertThat(response.capacidade()).isEqualTo(30);
        assertThat(response.status()).isEqualTo(StatusEstacionamento.MANUTENCAO);
    }

    @Test
    void delete_deveChamarRepositorio() {
        when(estacionamentoRepository.findById(5L)).thenReturn(Optional.of(estacionamento));

        estacionamentoService.delete(5L);

        verify(estacionamentoRepository).delete(estacionamento);
    }

    @Test
    void loadEntity_deveLancarResourceNotFoundQuandoNaoExistir() {
        when(estacionamentoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> estacionamentoService.loadEntity(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
