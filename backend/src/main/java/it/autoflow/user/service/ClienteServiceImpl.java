package it.autoflow.user.service;

import it.autoflow.authentication.utils.PasswordHasher;
import it.autoflow.commons.service.CrudService;
import it.autoflow.configuration.entity.Configurazione;
import it.autoflow.invoice.entity.Fattura;
import it.autoflow.proposal.entity.Proposta;
import it.autoflow.user.entity.Cliente;
import it.autoflow.user.entity.Ruolo;
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

        // campi ereditati da User
        if (entity.getUsername() == null) {
            entity.setUsername(entity.getEmail());
        }
        if (entity.getPassword() == null) {
            // password di default per i clienti (puoi cambiarla come vuoi)
            entity.setPassword(PasswordHasher.sha512("Cliente123!"));
        }
        if (entity.getRuolo() == null) {
            entity.setRuolo(Ruolo.CLIENTE);
        }
        if (!entity.isAttivo()) {
            entity.setAttivo(true);
        }

        return clienteRepository.save(entity);
    }


    @Override
    public Cliente getById(Long id) {
        return clienteRepository.findById(id).orElse(null);
    }

    @Override
    public Cliente update(Long id, Cliente entity) {
        Cliente existing = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente non trovato"));

        existing.setNome(entity.getNome());
        existing.setCognome(entity.getCognome());
        existing.setEmail(entity.getEmail());
        existing.setTelefono(entity.getTelefono());
        existing.setIndirizzo(entity.getIndirizzo());
        existing.setAttivo(entity.isAttivo());
        existing.setCodiceFiscale(entity.getCodiceFiscale());
        existing.setDataNascita(entity.getDataNascita());

        // se usi l'email come username, tienila allineata
        existing.setUsername(entity.getEmail());

        // password NON viene toccata qui
        return clienteRepository.save(existing);
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