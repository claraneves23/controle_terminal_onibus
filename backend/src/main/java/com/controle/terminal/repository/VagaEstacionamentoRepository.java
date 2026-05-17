package com.controle.terminal.repository;

import com.controle.terminal.domain.entity.VagaEstacionamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VagaEstacionamentoRepository extends JpaRepository<VagaEstacionamento, Long> {

    Page<VagaEstacionamento> findByEstacionamentoId(Long estacionamentoId, Pageable pageable);

    List<VagaEstacionamento> findByEstacionamentoIdOrderByCodigo(Long estacionamentoId);

    boolean existsByEstacionamentoIdAndCodigoIgnoreCase(Long estacionamentoId, String codigo);
}
