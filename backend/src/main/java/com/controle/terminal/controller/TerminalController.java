package com.controle.terminal.controller;

import com.controle.terminal.dto.request.TerminalRequest;
import com.controle.terminal.dto.response.PageResponse;
import com.controle.terminal.dto.response.TerminalResponse;
import com.controle.terminal.service.TerminalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/terminais")
@RequiredArgsConstructor
public class TerminalController {

    private final TerminalService terminalService;

    @GetMapping
    public PageResponse<TerminalResponse> list(@PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return terminalService.list(pageable);
    }

    @GetMapping("/{id}")
    public TerminalResponse findById(@PathVariable Long id) {
        return terminalService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<TerminalResponse> create(@Valid @RequestBody TerminalRequest request) {
        TerminalResponse created = terminalService.create(request);
        return ResponseEntity.created(URI.create("/api/terminais/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public TerminalResponse update(@PathVariable Long id, @Valid @RequestBody TerminalRequest request) {
        return terminalService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        terminalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
