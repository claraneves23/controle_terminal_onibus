package com.controle.terminal.controller;

import com.controle.terminal.dto.request.VeiculoRequest;
import com.controle.terminal.dto.response.PageResponse;
import com.controle.terminal.dto.response.VeiculoResponse;
import com.controle.terminal.service.VeiculoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/veiculos")
@RequiredArgsConstructor
public class VeiculoController {

    private final VeiculoService veiculoService;

    @GetMapping
    public PageResponse<VeiculoResponse> list(@RequestParam(required = false) String placa,
                                              @PageableDefault(size = 20, sort = "placa") Pageable pageable) {
        return veiculoService.list(placa, pageable);
    }

    @GetMapping("/{id}")
    public VeiculoResponse findById(@PathVariable Long id) {
        return veiculoService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR','OPERADOR')")
    public ResponseEntity<VeiculoResponse> create(@Valid @RequestBody VeiculoRequest request) {
        VeiculoResponse created = veiculoService.create(request);
        return ResponseEntity.created(URI.create("/api/veiculos/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR','OPERADOR')")
    public VeiculoResponse update(@PathVariable Long id, @Valid @RequestBody VeiculoRequest request) {
        return veiculoService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        veiculoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
