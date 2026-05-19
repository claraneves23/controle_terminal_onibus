package com.controle.terminal.service;

import com.controle.terminal.domain.entity.Estacionamento;
import com.controle.terminal.domain.entity.Terminal;
import com.controle.terminal.domain.entity.VagaEstacionamento;
import com.controle.terminal.domain.enums.StatusVaga;
import com.controle.terminal.dto.request.VagaRequest;
import com.controle.terminal.dto.request.VagaStatusRequest;
import com.controle.terminal.dto.response.VagaResponse;
import com.controle.terminal.exception.DuplicateResourceException;
import com.controle.terminal.exception.ResourceNotFoundException;
import com.controle.terminal.repository.VagaEstacionamentoRepository;
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
class VagaServiceTest {

    @Mock
    private VagaEstacionamentoRepository vagaRepository;

    @Mock
    private EstacionamentoService estacionamentoService;

    @InjectMocks
    private VagaService vagaService;

    private Estacionamento estacionamento;
    private VagaEstacionamento vaga;

    @BeforeEach
    void setUp() {
        Terminal terminal = Terminal.builder().id(1L).nome("T1").cidade("SP").ativo(true).build();
        estacionamento = Estacionamento.builder().id(5L).terminal(terminal).nome("Patio").capacidade(10).build();
        vaga = VagaEstacionamento.builder()
                .id(20L)
                .estacionamento(estacionamento)
                .codigo("V-01")
                .status(StatusVaga.LIVRE)
                .build();
    }

    @Test
    void create_deveDefinirStatusLivrePadrao() {
        VagaRequest request = new VagaRequest(5L, "V-02", null);
        when(estacionamentoService.loadEntity(5L)).thenReturn(estacionamento);
        when(vagaRepository.existsByEstacionamentoIdAndCodigoIgnoreCase(5L, "V-02")).thenReturn(false);
        when(vagaRepository.save(any(VagaEstacionamento.class))).thenAnswer(inv -> {
            VagaEstacionamento v = inv.getArgument(0);
            v.setId(21L);
            return v;
        });

        VagaResponse response = vagaService.create(request);

        assertThat(response.status()).isEqualTo(StatusVaga.LIVRE);
    }

    @Test
    void create_deveLancarDuplicateResourceQuandoCodigoJaExisteNoEstacionamento() {
        VagaRequest request = new VagaRequest(5L, "V-01", StatusVaga.LIVRE);
        when(estacionamentoService.loadEntity(5L)).thenReturn(estacionamento);
        when(vagaRepository.existsByEstacionamentoIdAndCodigoIgnoreCase(5L, "V-01")).thenReturn(true);

        assertThatThrownBy(() -> vagaService.create(request))
                .isInstanceOf(DuplicateResourceException.class);
        verify(vagaRepository, never()).save(any());
    }

    @Test
    void updateStatus_deveAtualizarStatusDaVaga() {
        when(vagaRepository.findById(20L)).thenReturn(Optional.of(vaga));

        VagaResponse response = vagaService.updateStatus(20L, new VagaStatusRequest(StatusVaga.RESERVADA));

        assertThat(response.status()).isEqualTo(StatusVaga.RESERVADA);
    }

    @Test
    void loadEntity_deveLancarResourceNotFoundQuandoNaoExistir() {
        when(vagaRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vagaService.loadEntity(404L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
