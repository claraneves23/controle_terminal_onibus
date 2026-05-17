package com.controle.terminal.repository;

import com.controle.terminal.domain.entity.Incidente;
import com.controle.terminal.domain.enums.NivelGravidade;
import com.controle.terminal.domain.enums.StatusIncidente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidenteRepository extends JpaRepository<Incidente, Long>, JpaSpecificationExecutor<Incidente> {

    long countByStatus(StatusIncidente status);

    @Query("""
            select i.tipo.nivelGravidade as nivel, count(i) as total
            from Incidente i
            where i.status = com.controle.terminal.domain.enums.StatusIncidente.ABERTO
              and (:terminalId is null or i.terminal.id = :terminalId)
            group by i.tipo.nivelGravidade
            """)
    List<NivelGravidadeCount> contagemAbertosPorGravidade(Long terminalId);

    interface NivelGravidadeCount {
        NivelGravidade getNivel();
        Long getTotal();
    }
}
