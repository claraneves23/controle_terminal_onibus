package com.controle.terminal.controller;

import com.controle.terminal.dto.request.UsuarioRequest;
import com.controle.terminal.dto.response.PageResponse;
import com.controle.terminal.dto.response.UsuarioResponse;
import com.controle.terminal.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public PageResponse<UsuarioResponse> list(@PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return usuarioService.list(pageable);
    }

    @GetMapping("/{id}")
    public UsuarioResponse findById(@PathVariable Long id) {
        return usuarioService.findById(id);
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> create(@Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse created = usuarioService.create(request);
        return ResponseEntity.created(URI.create("/api/usuarios/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public UsuarioResponse update(@PathVariable Long id, @Valid @RequestBody UsuarioRequest request) {
        return usuarioService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        usuarioService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
