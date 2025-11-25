package it.autoflow.proposal.repository;

import it.autoflow.proposal.entity.Proposta;
import it.autoflow.user.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropostaRepository extends JpaRepository<Proposta, Long> {

    List<Proposta> findByCliente(Cliente cliente);
}