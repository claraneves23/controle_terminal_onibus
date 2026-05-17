package com.controle.terminal.repository;

import com.controle.terminal.domain.entity.Veiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {

    Optional<Veiculo> findByPlacaIgnoreCase(String placa);

    boolean existsByPlacaIgnoreCase(String placa);

    Page<Veiculo> findByPlacaContainingIgnoreCase(String placa, Pageable pageable);
}
