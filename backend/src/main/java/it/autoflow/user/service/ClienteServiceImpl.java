package it.autoflow.user.service;

import it.autoflow.commons.service.CrudService;
import it.autoflow.configuration.entity.Configurazione;
import it.autoflow.invoice.entity.Fattura;
import it.autoflow.proposal.entity.Proposta;
import it.autoflow.user.entity.Cliente;
import it.autoflow.user.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService, CrudService<Cliente, Long> {

    private final ClienteRepository clienteRepository;

    @Override
    public Cliente create(Cliente entity) {
        return clienteRepository.save(entity);
    }

    @Override
    public Cliente getById(Long id) {
        return clienteRepository.findById(id).orElse(null);
    }

    @Override
    public Cliente update(Long id, Cliente entity) {
        entity.setId(id);
        return clienteRepository.save(entity);
    }

    @Override
    public boolean delete(Long id) {
        if (!clienteRepository.existsById(id)) {
            return false;
        }
        clienteRepository.deleteById(id);
        return true;
    }

    @Override
    public List<Cliente> findAll() {
        return clienteRepository.findAll();
    }

    @Override
    public List<Configurazione> getStoricoConfigurazioni(Long clienteId) {
        // TODO: usare ConfigurazioneRepository (es. findByClienteId)
        return Collections.emptyList();
    }

    @Override
    public List<Proposta> getStoricoProposte(Long clienteId) {
        // TODO: usare PropostaRepository (es. findByClienteId)
        return Collections.emptyList();
    }

    @Override
    public List<Fattura> getStoricoFatture(Long clienteId) {
        // TODO: usare FatturaRepository (es. findByClienteId)
        return Collections.emptyList();
    }
}