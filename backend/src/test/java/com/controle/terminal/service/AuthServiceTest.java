package com.controle.terminal.service;

import com.controle.terminal.domain.entity.Usuario;
import com.controle.terminal.domain.enums.PerfilUsuario;
import com.controle.terminal.dto.request.LoginRequest;
import com.controle.terminal.dto.response.TokenResponse;
import com.controle.terminal.exception.InvalidCredentialsException;
import com.controle.terminal.repository.UsuarioRepository;
import com.controle.terminal.security.JwtService;
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
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .nome("Admin")
                .email("admin@city.com")
                .perfil(PerfilUsuario.ADMINISTRADOR)
                .ativo(true)
                .senhaHash("HASH")
                .build();
    }

    @Test
    void login_deveRetornarTokenQuandoCredenciaisCorretas() {
        LoginRequest request = new LoginRequest("admin@city.com", "admin123");
        when(usuarioRepository.findByEmailIgnoreCase("admin@city.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("admin123", "HASH")).thenReturn(true);
        when(jwtService.generate(eq("admin@city.com"), anyMap())).thenReturn("token-jwt");
        when(jwtService.getExpirationMs()).thenReturn(86_400_000L);

        TokenResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("token-jwt");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.usuario().email()).isEqualTo("admin@city.com");
        assertThat(response.usuario().perfil()).isEqualTo(PerfilUsuario.ADMINISTRADOR);
        assertThat(usuario.getUltimoLogin()).isNotNull();
    }

    @Test
    void login_deveLancarInvalidCredentialsQuandoEmailNaoExiste() {
        LoginRequest request = new LoginRequest("ninguem@city.com", "qualquer");
        when(usuarioRepository.findByEmailIgnoreCase("ninguem@city.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtService, never()).generate(anyString(), anyMap());
    }

    @Test
    void login_deveLancarInvalidCredentialsQuandoUsuarioInativo() {
        usuario.setAtivo(false);
        LoginRequest request = new LoginRequest("admin@city.com", "admin123");
        when(usuarioRepository.findByEmailIgnoreCase("admin@city.com")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_deveLancarInvalidCredentialsQuandoSenhaErrada() {
        LoginRequest request = new LoginRequest("admin@city.com", "errada");
        when(usuarioRepository.findByEmailIgnoreCase("admin@city.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("errada", "HASH")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

}
