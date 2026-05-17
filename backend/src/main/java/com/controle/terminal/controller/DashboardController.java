package com.controle.terminal.controller;

import com.controle.terminal.dto.response.DashboardResponse;
import com.controle.terminal.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/resumo")
    public DashboardResponse resumo(@RequestParam(required = false) Long terminalId) {
        return dashboardService.resumo(terminalId);
    }
}
