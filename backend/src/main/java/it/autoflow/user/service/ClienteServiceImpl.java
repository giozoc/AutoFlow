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

        // --- TC2_10: account disattivato ---
        if (!existing.isAttivo()) {
            throw new IllegalStateException("Account disattivato");
        }

        String nome = entity.getNome();
        String cognome = entity.getCognome();
        String telefono = entity.getTelefono();

        // --- Validazione NOME (TC2_02, TC2_03, TC2_04) ---
        if (nome == null || nome.length() < 2) {
            throw new IllegalArgumentException("Nome troppo corto");
        }
        if (nome.length() > 30) {
            throw new IllegalArgumentException("Nome troppo lungo");
        }
        // Solo lettere e spazi (inclusi caratteri accentati)
        if (!nome.matches("^[A-Za-zÀ-ÖØ-öø-ÿ ]+$")) {
            throw new IllegalArgumentException("Formato nome non valido");
        }

        // --- Validazione COGNOME (TC2_05, TC2_06, TC2_07) ---
        if (cognome == null || cognome.length() < 2) {
            throw new IllegalArgumentException("Cognome troppo corto");
        }
        if (cognome.length() > 40) {
            throw new IllegalArgumentException("Cognome troppo lungo");
        }
        if (!cognome.matches("^[A-Za-zÀ-ÖØ-öø-ÿ ]+$")) {
            throw new IllegalArgumentException("Formato cognome non valido");
        }

        // --- Validazione TELEFONO (TC2_08, TC2_09) ---
        if (telefono == null || telefono.length() < 10) {
            throw new IllegalArgumentException("Numero di telefono non valido");
        }
        if (!telefono.matches("^[0-9]+$")) {
            throw new IllegalArgumentException("Formato telefono non valido");
        }

        // Se tutti i controlli passano, aggiorno l'entità
        existing.setNome(nome);
        existing.setCognome(cognome);
        existing.setEmail(entity.getEmail());
        existing.setTelefono(telefono);
        existing.setIndirizzo(entity.getIndirizzo());
        existing.setAttivo(entity.isAttivo());
        existing.setCodiceFiscale(entity.getCodiceFiscale());
        existing.setDataNascita(entity.getDataNascita());

        // se usi l'email come username, tienila allineata
        existing.setUsername(entity.getEmail());

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