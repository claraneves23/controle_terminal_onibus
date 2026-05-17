package com.controle.terminal.repository;

import com.controle.terminal.domain.entity.Terminal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TerminalRepository extends JpaRepository<Terminal, Long> {
    boolean existsByNomeIgnoreCase(String nome);
}
