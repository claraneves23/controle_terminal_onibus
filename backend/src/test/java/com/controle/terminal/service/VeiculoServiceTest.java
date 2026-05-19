package com.controle.terminal.service;

import com.controle.terminal.domain.entity.Veiculo;
import com.controle.terminal.domain.enums.TipoEmpresa;
import com.controle.terminal.domain.enums.TipoVeiculo;
import com.controle.terminal.dto.request.VeiculoRequest;
import com.controle.terminal.dto.response.VeiculoResponse;
import com.controle.terminal.exception.DuplicateResourceException;
import com.controle.terminal.exception.ResourceNotFoundException;
import com.controle.terminal.repository.VeiculoRepository;
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
class VeiculoServiceTest {

    @Mock
    private VeiculoRepository veiculoRepository;

    @InjectMocks
    private VeiculoService veiculoService;

    private Veiculo veiculo;

    @BeforeEach
    void setUp() {
        veiculo = Veiculo.builder()
                .id(7L)
                .placa("ABC1D23")
                .tipo(TipoVeiculo.CAMINHAO)
                .empresaResponsavel("Transp X")
                .tipoEmpresa(TipoEmpresa.TRANSPORTADORA)
                .modelo("VW 8160")
                .build();
    }

    @Test
    void create_deveNormalizarPlacaEmMaiusculas() {
        VeiculoRequest request = new VeiculoRequest("abc1d23", TipoVeiculo.CAMINHAO,
                "Transp Y", TipoEmpresa.CLIENTE, "Modelo Z");
        when(veiculoRepository.existsByPlacaIgnoreCase("ABC1D23")).thenReturn(false);
        when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(inv -> {
            Veiculo v = inv.getArgument(0);
            v.setId(8L);
            return v;
        });

        VeiculoResponse response = veiculoService.create(request);

        assertThat(response.placa()).isEqualTo("ABC1D23");
        assertThat(response.tipoEmpresa()).isEqualTo(TipoEmpresa.CLIENTE);
    }

    @Test
    void create_deveLancarDuplicateResourceQuandoPlacaJaExiste() {
        VeiculoRequest request = new VeiculoRequest("ABC1D23", TipoVeiculo.CAMINHAO,
                "Transp Y", TipoEmpresa.CLIENTE, null);
        when(veiculoRepository.existsByPlacaIgnoreCase("ABC1D23")).thenReturn(true);

        assertThatThrownBy(() -> veiculoService.create(request))
                .isInstanceOf(DuplicateResourceException.class);
        verify(veiculoRepository, never()).save(any());
    }

    @Test
    void update_devePermitirManterMesmaPlaca() {
        VeiculoRequest request = new VeiculoRequest("ABC1D23", TipoVeiculo.CARRETA,
                "Transp X", TipoEmpresa.TRANSPORTADORA, "MB Actros");
        when(veiculoRepository.findById(7L)).thenReturn(Optional.of(veiculo));

        VeiculoResponse response = veiculoService.update(7L, request);

        assertThat(response.tipo()).isEqualTo(TipoVeiculo.CARRETA);
        assertThat(response.modelo()).isEqualTo("MB Actros");
        verify(veiculoRepository, never()).existsByPlacaIgnoreCase(any());
    }

    @Test
    void loadEntity_deveLancarResourceNotFoundQuandoNaoExistir() {
        when(veiculoRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> veiculoService.loadEntity(404L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findOrCreateByPlaca_deveCriarQuandoPlacaInexistente() {
        VeiculoRequest request = new VeiculoRequest("XYZ9A88", TipoVeiculo.VAN,
                "Empresa B", TipoEmpresa.FORNECEDOR, null);
        when(veiculoRepository.findByPlacaIgnoreCase("XYZ9A88")).thenReturn(Optional.empty());
        when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(inv -> inv.getArgument(0));

        Veiculo created = veiculoService.findOrCreateByPlaca(request);

        assertThat(created.getPlaca()).isEqualTo("XYZ9A88");
    }
}
