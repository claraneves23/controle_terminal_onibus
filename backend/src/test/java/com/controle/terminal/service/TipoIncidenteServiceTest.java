package com.controle.terminal.service;

import com.controle.terminal.domain.entity.TipoIncidente;
import com.controle.terminal.domain.enums.NivelGravidade;
import com.controle.terminal.dto.request.TipoIncidenteRequest;
import com.controle.terminal.dto.response.TipoIncidenteResponse;
import com.controle.terminal.exception.DuplicateResourceException;
import com.controle.terminal.exception.ResourceNotFoundException;
import com.controle.terminal.repository.TipoIncidenteRepository;
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
class TipoIncidenteServiceTest {

    @Mock
    private TipoIncidenteRepository tipoIncidenteRepository;

    @InjectMocks
    private TipoIncidenteService tipoIncidenteService;

    private TipoIncidente tipo;

    @BeforeEach
    void setUp() {
        tipo = TipoIncidente.builder()
                .id(1L)
                .nome("Vazamento")
                .descricao("Vazamento de carga liquida")
                .nivelGravidade(NivelGravidade.ALTO)
                .build();
    }

    @Test
    void create_deveSalvarTipoIncidente() {
        TipoIncidenteRequest request = new TipoIncidenteRequest("Avaria", "Avaria fisica", NivelGravidade.MEDIO);
        when(tipoIncidenteRepository.existsByNomeIgnoreCase("Avaria")).thenReturn(false);
        when(tipoIncidenteRepository.save(any(TipoIncidente.class))).thenAnswer(inv -> {
            TipoIncidente t = inv.getArgument(0);
            t.setId(2L);
            return t;
        });

        TipoIncidenteResponse response = tipoIncidenteService.create(request);

        assertThat(response.id()).isEqualTo(2L);
        assertThat(response.nivelGravidade()).isEqualTo(NivelGravidade.MEDIO);
    }

    @Test
    void create_deveLancarDuplicateResourceQuandoNomeJaExiste() {
        TipoIncidenteRequest request = new TipoIncidenteRequest("Vazamento", null, NivelGravidade.ALTO);
        when(tipoIncidenteRepository.existsByNomeIgnoreCase("Vazamento")).thenReturn(true);

        assertThatThrownBy(() -> tipoIncidenteService.create(request))
                .isInstanceOf(DuplicateResourceException.class);
        verify(tipoIncidenteRepository, never()).save(any());
    }

    @Test
    void update_devePermitirManterMesmoNome() {
        TipoIncidenteRequest request = new TipoIncidenteRequest("Vazamento", "Nova descricao", NivelGravidade.CRITICO);
        when(tipoIncidenteRepository.findById(1L)).thenReturn(Optional.of(tipo));

        TipoIncidenteResponse response = tipoIncidenteService.update(1L, request);

        assertThat(response.descricao()).isEqualTo("Nova descricao");
        assertThat(response.nivelGravidade()).isEqualTo(NivelGravidade.CRITICO);
        verify(tipoIncidenteRepository, never()).existsByNomeIgnoreCase(any());
    }

    @Test
    void loadEntity_deveLancarResourceNotFoundQuandoNaoExistir() {
        when(tipoIncidenteRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tipoIncidenteService.loadEntity(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
