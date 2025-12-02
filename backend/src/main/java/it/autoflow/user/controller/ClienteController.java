package it.autoflow.user.controller;

import it.autoflow.configuration.entity.Configurazione;
import it.autoflow.invoice.entity.Fattura;
import it.autoflow.proposal.entity.Proposta;
import it.autoflow.user.dto.ClienteDTO;
import it.autoflow.user.entity.Cliente;
import it.autoflow.user.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/clienti")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    public List<ClienteDTO> getAll() {
        return clienteService.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ClienteDTO getById(@PathVariable Long id) {
        Cliente cliente = clienteService.getById(id);
        return cliente != null ? toDto(cliente) : null;
    }

    @PostMapping
    public ClienteDTO create(@RequestBody ClienteDTO dto) {
        Cliente saved = clienteService.create(toEntity(dto));
        return toDto(saved);
    }

    @PutMapping("/{id}")
    public ClienteDTO update(@PathVariable Long id, @RequestBody ClienteDTO dto) {
        Cliente updated = clienteService.update(id, toEntity(dto));
        return toDto(updated);
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return clienteService.delete(id);
    }

    @GetMapping("/{id}/configurazioni")
    public List<Configurazione> getConfigurazioni(@PathVariable Long id) {
        return clienteService.getStoricoConfigurazioni(id);
    }

    @GetMapping("/{id}/proposte")
    public List<Proposta> getProposte(@PathVariable Long id) {
        return clienteService.getStoricoProposte(id);
    }

    @GetMapping("/{id}/fatture")
    public List<Fattura> getFatture(@PathVariable Long id) {
        return clienteService.getStoricoFatture(id);
    }

    // ---------- mapping helper ----------

    private ClienteDTO toDto(Cliente c) {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(c.getId());
        dto.setNome(c.getNome());
        dto.setCognome(c.getCognome());
        dto.setEmail(c.getEmail());
        dto.setTelefono(c.getTelefono());
        dto.setIndirizzo(c.getIndirizzo());
        dto.setAttivo(c.isAttivo());
        dto.setDataNascita(c.getDataNascita());
        dto.setCodiceFiscale(c.getCodiceFiscale());
        return dto;
    }

    private Cliente toEntity(ClienteDTO dto) {
        Cliente c = new Cliente();
        c.setId(dto.getId());
        c.setNome(dto.getNome());
        c.setCognome(dto.getCognome());
        c.setEmail(dto.getEmail());
        c.setTelefono(dto.getTelefono());
        c.setIndirizzo(dto.getIndirizzo());
        c.setAttivo(dto.isAttivo());
        c.setDataNascita(dto.getDataNascita());      // 🔴 CORRETTO
        c.setCodiceFiscale(dto.getCodiceFiscale());  // 🔴 CORRETTO
        return c;
    }
}