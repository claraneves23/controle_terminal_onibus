package com.controle.terminal.controller;

import com.controle.terminal.dto.request.LoginRequest;
import com.controle.terminal.dto.response.TokenResponse;
import com.controle.terminal.security.AuthenticatedUser;
import com.controle.terminal.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "nome", user.getNome(),
                "email", user.getEmail(),
                "perfil", user.getPerfil()
        ));
    }
}
