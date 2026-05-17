package com.controle.terminal.controller;

import com.controle.terminal.domain.enums.NivelGravidade;
import com.controle.terminal.domain.enums.StatusIncidente;
import com.controle.terminal.dto.request.EncerrarIncidenteRequest;
import com.controle.terminal.dto.request.IncidenteRequest;
import com.controle.terminal.dto.response.IncidenteResponse;
import com.controle.terminal.dto.response.PageResponse;
import com.controle.terminal.service.IncidenteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/incidentes")
@RequiredArgsConstructor
public class IncidenteController {

    private final IncidenteService incidenteService;

    @GetMapping
    public PageResponse<IncidenteResponse> list(
            @RequestParam(required = false) StatusIncidente status,
            @RequestParam(required = false) Long terminalId,
            @RequestParam(required = false) NivelGravidade gravidade,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ate,
            @PageableDefault(size = 20, sort = "ocorridoEm", direction = Sort.Direction.DESC) Pageable pageable) {
        return incidenteService.list(status, terminalId, gravidade, de, ate, pageable);
    }

    @GetMapping("/{id}")
    public IncidenteResponse findById(@PathVariable Long id) {
        return incidenteService.findById(id);
    }

    @PostMapping
    public ResponseEntity<IncidenteResponse> create(@Valid @RequestBody IncidenteRequest request) {
        IncidenteResponse created = incidenteService.create(request);
        return ResponseEntity.created(URI.create("/api/incidentes/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
    public IncidenteResponse update(@PathVariable Long id, @Valid @RequestBody IncidenteRequest request) {
        return incidenteService.update(id, request);
    }

    @PatchMapping("/{id}/encerrar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
    public IncidenteResponse encerrar(@PathVariable Long id, @Valid @RequestBody EncerrarIncidenteRequest request) {
        return incidenteService.encerrar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        incidenteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
