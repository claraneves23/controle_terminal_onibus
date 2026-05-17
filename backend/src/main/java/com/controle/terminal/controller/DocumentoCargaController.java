package com.controle.terminal.controller;

import com.controle.terminal.dto.request.DocumentoCargaRequest;
import com.controle.terminal.dto.response.DocumentoCargaResponse;
import com.controle.terminal.service.DocumentoCargaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class DocumentoCargaController {

    private final DocumentoCargaService documentoService;

    @GetMapping("/api/operacoes/{operacaoId}/documentos")
    public List<DocumentoCargaResponse> list(@PathVariable Long operacaoId) {
        return documentoService.listByOperacao(operacaoId);
    }

    @PostMapping("/api/operacoes/{operacaoId}/documentos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR','OPERADOR')")
    public ResponseEntity<DocumentoCargaResponse> create(@PathVariable Long operacaoId,
                                                         @Valid @RequestBody DocumentoCargaRequest request) {
        DocumentoCargaResponse created = documentoService.create(operacaoId, request);
        return ResponseEntity.created(URI.create("/api/documentos/" + created.id())).body(created);
    }

    @PutMapping("/api/documentos/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR','OPERADOR')")
    public DocumentoCargaResponse update(@PathVariable Long id, @Valid @RequestBody DocumentoCargaRequest request) {
        return documentoService.update(id, request);
    }

    @DeleteMapping("/api/documentos/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        documentoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
