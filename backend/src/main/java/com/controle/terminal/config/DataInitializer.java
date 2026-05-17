package com.controle.terminal.config;

import com.controle.terminal.domain.entity.Usuario;
import com.controle.terminal.domain.enums.PerfilUsuario;
import com.controle.terminal.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }

        log.info("Inicializando usuarios padrao...");

        List<Usuario> defaults = List.of(
                build("Administrador",        "admin@city.com",      "admin123", PerfilUsuario.ADMINISTRADOR),
                build("Supervisor Operacoes", "supervisor@city.com", "sup123",   PerfilUsuario.SUPERVISOR),
                build("Operador de Patio",    "operador@city.com",   "oper123",  PerfilUsuario.OPERADOR),
                build("Seguranca da Guarita", "seguranca@city.com",  "seg123",   PerfilUsuario.SEGURANCA)
        );
        usuarioRepository.saveAll(defaults);

        log.info("Usuarios padrao criados: {}", defaults.stream().map(Usuario::getEmail).toList());
    }

    private Usuario build(String nome, String email, String senha, PerfilUsuario perfil) {
        return Usuario.builder()
                .nome(nome)
                .email(email)
                .perfil(perfil)
                .ativo(true)
                .senhaHash(passwordEncoder.encode(senha))
                .build();
    }
}
