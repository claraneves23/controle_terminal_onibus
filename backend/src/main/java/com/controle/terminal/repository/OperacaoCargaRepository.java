package com.controle.terminal.repository;

import com.controle.terminal.domain.entity.OperacaoCarga;
import com.controle.terminal.domain.enums.StatusOperacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OperacaoCargaRepository extends JpaRepository<OperacaoCarga, Long>, JpaSpecificationExecutor<OperacaoCarga> {

    Page<OperacaoCarga> findByStatus(StatusOperacao status, Pageable pageable);

    long countByStatus(StatusOperacao status);

    long countByTerminalIdAndStatus(Long terminalId, StatusOperacao status);

    @Query("""
            select count(o) from OperacaoCarga o
            where o.status = com.controle.terminal.domain.enums.StatusOperacao.FINALIZADA
              and o.finalizadaEm >= :inicio
              and (:terminalId is null or o.terminal.id = :terminalId)
            """)
    long countFinalizadasDesde(LocalDateTime inicio, Long terminalId);

    @Query(value = """
            select avg(extract(epoch from (o.dt_fim - o.dt_inicio)))
            from operacao_carga o
            where o.status_operacao = 'FINALIZADA'
              and o.dt_inicio is not null and o.dt_fim is not null
              and o.dt_fim >= :inicio
              and (cast(:terminalId as bigint) is null or o.id_terminal = :terminalId)
            """, nativeQuery = true)
    Double tempoMedioSegundosDesde(LocalDateTime inicio, Long terminalId);

    @Query("""
            select o from OperacaoCarga o
            where o.status in :statuses
            order by coalesce(o.iniciadaEm, o.agendadaEm) desc
            """)
    List<OperacaoCarga> findRecentByStatuses(List<StatusOperacao> statuses, Pageable pageable);
}
