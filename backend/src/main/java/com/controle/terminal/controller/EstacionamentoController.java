package com.controle.terminal.controller;

import com.controle.terminal.dto.request.EstacionamentoRequest;
import com.controle.terminal.dto.response.EstacionamentoResponse;
import com.controle.terminal.dto.response.PageResponse;
import com.controle.terminal.service.EstacionamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/estacionamentos")
@RequiredArgsConstructor
public class EstacionamentoController {

    private final EstacionamentoService estacionamentoService;

    @GetMapping
    public PageResponse<EstacionamentoResponse> list(@RequestParam(required = false) Long terminalId,
                                                     @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return estacionamentoService.list(terminalId, pageable);
    }

    @GetMapping("/{id}")
    public EstacionamentoResponse findById(@PathVariable Long id) {
        return estacionamentoService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
    public ResponseEntity<EstacionamentoResponse> create(@Valid @RequestBody EstacionamentoRequest request) {
        EstacionamentoResponse created = estacionamentoService.create(request);
        return ResponseEntity.created(URI.create("/api/estacionamentos/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
    public EstacionamentoResponse update(@PathVariable Long id, @Valid @RequestBody EstacionamentoRequest request) {
        return estacionamentoService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        estacionamentoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
