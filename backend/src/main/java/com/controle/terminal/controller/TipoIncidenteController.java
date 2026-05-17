package com.controle.terminal.controller;

import com.controle.terminal.dto.request.TipoIncidenteRequest;
import com.controle.terminal.dto.response.PageResponse;
import com.controle.terminal.dto.response.TipoIncidenteResponse;
import com.controle.terminal.service.TipoIncidenteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/tipos-incidente")
@RequiredArgsConstructor
public class TipoIncidenteController {

    private final TipoIncidenteService tipoIncidenteService;

    @GetMapping
    public PageResponse<TipoIncidenteResponse> list(@PageableDefault(size = 50, sort = "nome") Pageable pageable) {
        return tipoIncidenteService.list(pageable);
    }

    @GetMapping("/{id}")
    public TipoIncidenteResponse findById(@PathVariable Long id) {
        return tipoIncidenteService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<TipoIncidenteResponse> create(@Valid @RequestBody TipoIncidenteRequest request) {
        TipoIncidenteResponse created = tipoIncidenteService.create(request);
        return ResponseEntity.created(URI.create("/api/tipos-incidente/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public TipoIncidenteResponse update(@PathVariable Long id, @Valid @RequestBody TipoIncidenteRequest request) {
        return tipoIncidenteService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tipoIncidenteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
