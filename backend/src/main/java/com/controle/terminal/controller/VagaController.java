package com.controle.terminal.controller;

import com.controle.terminal.dto.request.VagaRequest;
import com.controle.terminal.dto.request.VagaStatusRequest;
import com.controle.terminal.dto.response.PageResponse;
import com.controle.terminal.dto.response.VagaResponse;
import com.controle.terminal.service.VagaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/vagas")
@RequiredArgsConstructor
public class VagaController {

    private final VagaService vagaService;

    @GetMapping
    public PageResponse<VagaResponse> list(@RequestParam(required = false) Long estacionamentoId,
                                           @PageableDefault(size = 50, sort = "codigo") Pageable pageable) {
        return vagaService.list(estacionamentoId, pageable);
    }

    @GetMapping("/{id}")
    public VagaResponse findById(@PathVariable Long id) {
        return vagaService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
    public ResponseEntity<VagaResponse> create(@Valid @RequestBody VagaRequest request) {
        VagaResponse created = vagaService.create(request);
        return ResponseEntity.created(URI.create("/api/vagas/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
    public VagaResponse update(@PathVariable Long id, @Valid @RequestBody VagaRequest request) {
        return vagaService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR','OPERADOR')")
    public VagaResponse updateStatus(@PathVariable Long id, @Valid @RequestBody VagaStatusRequest request) {
        return vagaService.updateStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vagaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
