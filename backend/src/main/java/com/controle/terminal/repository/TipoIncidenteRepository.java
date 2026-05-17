package com.controle.terminal.repository;

import com.controle.terminal.domain.entity.TipoIncidente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoIncidenteRepository extends JpaRepository<TipoIncidente, Long> {

    boolean existsByNomeIgnoreCase(String nome);
}
