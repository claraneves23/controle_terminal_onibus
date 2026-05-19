package com.controle.terminal.service;

import com.controle.terminal.domain.entity.Terminal;
import com.controle.terminal.dto.request.TerminalRequest;
import com.controle.terminal.dto.response.TerminalResponse;
import com.controle.terminal.exception.DuplicateResourceException;
import com.controle.terminal.exception.ResourceNotFoundException;
import com.controle.terminal.repository.TerminalRepository;
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
class TerminalServiceTest {

    @Mock
    private TerminalRepository terminalRepository;

    @InjectMocks
    private TerminalService terminalService;

    private Terminal terminalExistente;

    @BeforeEach
    void setUp() {
        terminalExistente = Terminal.builder()
                .id(1L)
                .nome("Terminal Central")
                .endereco("Rua A, 100")
                .cidade("Sao Paulo")
                .ativo(true)
                .build();
    }

    @Test
    void create_deveSalvarTerminalComAtivoTruePadraoQuandoNaoInformado() {
        TerminalRequest request = new TerminalRequest("Terminal Novo", "Rua B", "Campinas", null);
        when(terminalRepository.existsByNomeIgnoreCase("Terminal Novo")).thenReturn(false);
        when(terminalRepository.save(any(Terminal.class))).thenAnswer(invocation -> {
            Terminal t = invocation.getArgument(0);
            t.setId(2L);
            return t;
        });

        TerminalResponse response = terminalService.create(request);

        assertThat(response.id()).isEqualTo(2L);
        assertThat(response.nome()).isEqualTo("Terminal Novo");
        assertThat(response.ativo()).isTrue();
    }

    @Test
    void create_deveLancarDuplicateResourceExceptionQuandoNomeJaExiste() {
        TerminalRequest request = new TerminalRequest("Terminal Central", null, "Sao Paulo", true);
        when(terminalRepository.existsByNomeIgnoreCase("Terminal Central")).thenReturn(true);

        assertThatThrownBy(() -> terminalService.create(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(terminalRepository, never()).save(any());
    }

    @Test
    void findById_deveLancarResourceNotFoundExceptionQuandoIdInexistente() {
        when(terminalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> terminalService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_devePermitirManterMesmoNome() {
        TerminalRequest request = new TerminalRequest("Terminal Central", "Rua Z, 200", "Sao Paulo", true);
        when(terminalRepository.findById(1L)).thenReturn(Optional.of(terminalExistente));

        TerminalResponse response = terminalService.update(1L, request);

        assertThat(response.endereco()).isEqualTo("Rua Z, 200");
        verify(terminalRepository, never()).existsByNomeIgnoreCase(any());
    }

    @Test
    void update_deveLancarDuplicateResourceExceptionAoTrocarParaNomeUsadoPorOutro() {
        TerminalRequest request = new TerminalRequest("Outro Terminal", null, "Sao Paulo", true);
        when(terminalRepository.findById(1L)).thenReturn(Optional.of(terminalExistente));
        when(terminalRepository.existsByNomeIgnoreCase("Outro Terminal")).thenReturn(true);

        assertThatThrownBy(() -> terminalService.update(1L, request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void delete_deveRemoverQuandoIdExiste() {
        when(terminalRepository.findById(1L)).thenReturn(Optional.of(terminalExistente));

        terminalService.delete(1L);

        verify(terminalRepository).delete(terminalExistente);
    }
}
