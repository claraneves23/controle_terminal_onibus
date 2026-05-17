package com.controle.terminal.repository;

import com.controle.terminal.domain.entity.Estacionamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstacionamentoRepository extends JpaRepository<Estacionamento, Long> {

    Page<Estacionamento> findByTerminalId(Long terminalId, Pageable pageable);
}
