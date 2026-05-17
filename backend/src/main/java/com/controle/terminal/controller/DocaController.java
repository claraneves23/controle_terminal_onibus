package com.controle.terminal.controller;

import com.controle.terminal.dto.request.DocaRequest;
import com.controle.terminal.dto.request.DocaStatusRequest;
import com.controle.terminal.dto.response.DocaResponse;
import com.controle.terminal.dto.response.PageResponse;
import com.controle.terminal.service.DocaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/docas")
@RequiredArgsConstructor
public class DocaController {

    private final DocaService docaService;

    @GetMapping
    public PageResponse<DocaResponse> list(@RequestParam(required = false) Long terminalId,
                                           @PageableDefault(size = 50, sort = "codigo") Pageable pageable) {
        return docaService.list(terminalId, pageable);
    }

    @GetMapping("/por-terminal/{terminalId}")
    public List<DocaResponse> listByTerminal(@PathVariable Long terminalId) {
        return docaService.listByTerminal(terminalId);
    }

    @GetMapping("/{id}")
    public DocaResponse findById(@PathVariable Long id) {
        return docaService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
    public ResponseEntity<DocaResponse> create(@Valid @RequestBody DocaRequest request) {
        DocaResponse created = docaService.create(request);
        return ResponseEntity.created(URI.create("/api/docas/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
    public DocaResponse update(@PathVariable Long id, @Valid @RequestBody DocaRequest request) {
        return docaService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR','OPERADOR')")
    public DocaResponse updateStatus(@PathVariable Long id, @Valid @RequestBody DocaStatusRequest request) {
        return docaService.updateStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        docaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
