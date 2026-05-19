package com.controle.terminal.service;

import com.controle.terminal.domain.entity.Usuario;
import com.controle.terminal.domain.enums.PerfilUsuario;
import com.controle.terminal.dto.request.UsuarioRequest;
import com.controle.terminal.dto.response.UsuarioResponse;
import com.controle.terminal.exception.BusinessException;
import com.controle.terminal.exception.DuplicateResourceException;
import com.controle.terminal.exception.ResourceNotFoundException;
import com.controle.terminal.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(3L)
                .nome("Maria")
                .email("maria@city.com")
                .perfil(PerfilUsuario.OPERADOR)
                .ativo(true)
                .senhaHash("hash-antigo")
                .build();
    }

    @Test
    void create_deveLancarBusinessExceptionQuandoSenhaAusente() {
        UsuarioRequest request = new UsuarioRequest("Joao", "joao@city.com",
                PerfilUsuario.OPERADOR, true, null);

        assertThatThrownBy(() -> usuarioService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("senha");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void create_deveLancarDuplicateResourceQuandoEmailJaExiste() {
        UsuarioRequest request = new UsuarioRequest("Joao", "maria@city.com",
                PerfilUsuario.OPERADOR, true, "senha1234");
        when(usuarioRepository.existsByEmailIgnoreCase("maria@city.com")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.create(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void create_deveCodificarSenhaAntesDeSalvar() {
        UsuarioRequest request = new UsuarioRequest("Joao", "joao@city.com",
                PerfilUsuario.OPERADOR, true, "senha1234");
        when(usuarioRepository.existsByEmailIgnoreCase("joao@city.com")).thenReturn(false);
        when(passwordEncoder.encode("senha1234")).thenReturn("HASH");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(99L);
            return u;
        });

        UsuarioResponse response = usuarioService.create(request);

        assertThat(response.id()).isEqualTo(99L);
        verify(passwordEncoder).encode("senha1234");
    }

    @Test
    void update_naoDeveAlterarSenhaQuandoCampoVazio() {
        UsuarioRequest request = new UsuarioRequest("Maria Atualizada", "maria@city.com",
                PerfilUsuario.SUPERVISOR, true, null);
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario));

        usuarioService.update(3L, request);

        assertThat(usuario.getNome()).isEqualTo("Maria Atualizada");
        assertThat(usuario.getPerfil()).isEqualTo(PerfilUsuario.SUPERVISOR);
        assertThat(usuario.getSenhaHash()).isEqualTo("hash-antigo");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void update_deveTrocarSenhaQuandoFornecida() {
        UsuarioRequest request = new UsuarioRequest("Maria", "maria@city.com",
                PerfilUsuario.OPERADOR, true, "novasenha");
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("novasenha")).thenReturn("HASH-NOVO");

        usuarioService.update(3L, request);

        assertThat(usuario.getSenhaHash()).isEqualTo("HASH-NOVO");
    }

    @Test
    void update_deveLancarDuplicateResourceAoTrocarParaEmailJaUsado() {
        UsuarioRequest request = new UsuarioRequest("Maria", "outro@city.com",
                PerfilUsuario.OPERADOR, true, null);
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmailIgnoreCase("outro@city.com")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.update(3L, request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void loadEntity_deveLancarResourceNotFoundQuandoNaoExistir() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.loadEntity(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
