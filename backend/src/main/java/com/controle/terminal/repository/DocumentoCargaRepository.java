package com.controle.terminal.repository;

import com.controle.terminal.domain.entity.DocumentoCarga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentoCargaRepository extends JpaRepository<DocumentoCarga, Long> {

    List<DocumentoCarga> findByOperacaoIdOrderById(Long operacaoId);
}
