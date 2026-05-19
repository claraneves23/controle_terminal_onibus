package com.controle.terminal.service;

import com.controle.terminal.domain.entity.DocumentoCarga;
import com.controle.terminal.domain.entity.OperacaoCarga;
import com.controle.terminal.domain.enums.TipoDocumento;
import com.controle.terminal.dto.request.DocumentoCargaRequest;
import com.controle.terminal.dto.response.DocumentoCargaResponse;
import com.controle.terminal.exception.ResourceNotFoundException;
import com.controle.terminal.repository.DocumentoCargaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentoCargaServiceTest {

    @Mock
    private DocumentoCargaRepository documentoRepository;

    @Mock
    private OperacaoService operacaoService;

    @InjectMocks
    private DocumentoCargaService documentoCargaService;

    private OperacaoCarga operacao;
    private DocumentoCarga documento;

    @BeforeEach
    void setUp() {
        operacao = OperacaoCarga.builder().id(100L).build();
        documento = DocumentoCarga.builder()
                .id(500L)
                .operacao(operacao)
                .tipo(TipoDocumento.NOTA_FISCAL)
                .numero("NF-001")
                .build();
    }

    @Test
    void create_deveVincularDocumentoAOperacao() {
        DocumentoCargaRequest request = new DocumentoCargaRequest(
                TipoDocumento.ROMANEIO, "RM-77", LocalDate.now(), "Obs");
        when(operacaoService.loadEntity(100L)).thenReturn(operacao);
        when(documentoRepository.save(any(DocumentoCarga.class))).thenAnswer(inv -> {
            DocumentoCarga d = inv.getArgument(0);
            d.setId(501L);
            return d;
        });

        DocumentoCargaResponse response = documentoCargaService.create(100L, request);

        assertThat(response.id()).isEqualTo(501L);
        assertThat(response.tipo()).isEqualTo(TipoDocumento.ROMANEIO);
        assertThat(response.numero()).isEqualTo("RM-77");
    }

    @Test
    void update_deveAtualizarCampos() {
        DocumentoCargaRequest request = new DocumentoCargaRequest(
                TipoDocumento.CONHECIMENTO_TRANSPORTE, "CTE-999", LocalDate.now(), "novo obs");
        when(documentoRepository.findById(500L)).thenReturn(Optional.of(documento));

        DocumentoCargaResponse response = documentoCargaService.update(500L, request);

        assertThat(response.tipo()).isEqualTo(TipoDocumento.CONHECIMENTO_TRANSPORTE);
        assertThat(response.numero()).isEqualTo("CTE-999");
        assertThat(documento.getObservacao()).isEqualTo("novo obs");
    }

    @Test
    void delete_deveRemoverQuandoIdExiste() {
        when(documentoRepository.findById(500L)).thenReturn(Optional.of(documento));

        documentoCargaService.delete(500L);

        verify(documentoRepository).delete(documento);
    }

    @Test
    void delete_deveLancarResourceNotFoundQuandoIdInexistente() {
        when(documentoRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentoCargaService.delete(404L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
