package it.autoflow.user.service;

import it.autoflow.commons.service.CrudService;
import it.autoflow.configuration.entity.Configurazione;
import it.autoflow.invoice.entity.Fattura;
import it.autoflow.proposal.entity.Proposta;
import it.autoflow.user.entity.Cliente;

import java.util.List;

public interface ClienteService extends CrudService<Cliente, Long> {

    List<Configurazione> getStoricoConfigurazioni(Long clienteId);

    List<Proposta> getStoricoProposte(Long clienteId);

    List<Fattura> getStoricoFatture(Long clienteId);
}