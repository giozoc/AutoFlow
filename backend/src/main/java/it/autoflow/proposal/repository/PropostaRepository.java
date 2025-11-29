package it.autoflow.proposal.repository;

import it.autoflow.proposal.entity.Proposta;
import it.autoflow.proposal.entity.StatoProposta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropostaRepository extends JpaRepository<Proposta, Long> {

    List<Proposta> findByCliente_Id(Long clienteId);

    List<Proposta> findByAddettoVendite_Id(Long addettoVenditeId);

    long countByStato(StatoProposta stato);
}