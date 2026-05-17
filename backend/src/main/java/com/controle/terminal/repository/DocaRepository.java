package com.controle.terminal.repository;

import com.controle.terminal.domain.entity.Doca;
import com.controle.terminal.domain.enums.StatusDoca;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocaRepository extends JpaRepository<Doca, Long> {

    Page<Doca> findByTerminalId(Long terminalId, Pageable pageable);

    List<Doca> findByTerminalIdOrderByCodigo(Long terminalId);

    boolean existsByTerminalIdAndCodigoIgnoreCase(Long terminalId, String codigo);

    long countByStatus(StatusDoca status);

    long countByTerminalIdAndStatus(Long terminalId, StatusDoca status);
}
