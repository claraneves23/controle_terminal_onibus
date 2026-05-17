package com.controle.terminal.controller;

import com.controle.terminal.domain.enums.StatusOperacao;
import com.controle.terminal.dto.request.CheckinRequest;
import com.controle.terminal.dto.response.OperacaoResponse;
import com.controle.terminal.dto.response.OperacaoResumoResponse;
import com.controle.terminal.dto.response.PageResponse;
import com.controle.terminal.service.OperacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/operacoes")
@RequiredArgsConstructor
public class OperacaoController {

    private final OperacaoService operacaoService;

    @GetMapping
    public PageResponse<OperacaoResumoResponse> list(
            @RequestParam(required = false) StatusOperacao status,
            @RequestParam(required = false) Long terminalId,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return operacaoService.list(status, terminalId, pageable);
    }

    @GetMapping("/{id}")
    public OperacaoResponse findById(@PathVariable Long id) {
        return operacaoService.findById(id);
    }

    @PostMapping("/checkin")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR','OPERADOR','SEGURANCA')")
    public ResponseEntity<OperacaoResponse> checkin(@Valid @RequestBody CheckinRequest request) {
        OperacaoResponse created = operacaoService.checkin(request);
        return ResponseEntity.created(URI.create("/api/operacoes/" + created.id())).body(created);
    }

    @PatchMapping("/{id}/iniciar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR','OPERADOR')")
    public OperacaoResponse iniciar(@PathVariable Long id) {
        return operacaoService.iniciar(id);
    }

    @PatchMapping("/{id}/finalizar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR','OPERADOR')")
    public OperacaoResponse finalizar(@PathVariable Long id) {
        return operacaoService.finalizar(id);
    }

    @PatchMapping("/{id}/checkout")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR','OPERADOR','SEGURANCA')")
    public OperacaoResponse checkout(@PathVariable Long id) {
        return operacaoService.checkout(id);
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
    public OperacaoResponse cancelar(@PathVariable Long id) {
        return operacaoService.cancelar(id);
    }
}
