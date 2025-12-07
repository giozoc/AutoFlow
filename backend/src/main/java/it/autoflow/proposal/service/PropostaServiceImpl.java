package it.autoflow.proposal.service;

import it.autoflow.configuration.entity.Configurazione;
import it.autoflow.configuration.repository.ConfigurazioneRepository;
import it.autoflow.proposal.dto.PropostaDTO;
import it.autoflow.proposal.entity.Proposta;
import it.autoflow.proposal.repository.PropostaRepository;
import it.autoflow.proposal.service.PropostaService;
import it.autoflow.user.entity.AddettoVendite;
import it.autoflow.user.entity.Cliente;
import it.autoflow.user.repository.AddettoVenditeRepository;
import it.autoflow.user.repository.ClienteRepository;
import it.autoflow.vehicle.entity.StatoVeicolo;
import it.autoflow.vehicle.entity.Veicolo;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PropostaServiceImpl implements PropostaService {

    private final PropostaRepository propostaRepository;
    private final ClienteRepository clienteRepository;
    private final AddettoVenditeRepository addettoVenditeRepository;
    private final ConfigurazioneRepository configurazioneRepository;

    public PropostaServiceImpl(PropostaRepository propostaRepository,
                               ClienteRepository clienteRepository,
                               AddettoVenditeRepository addettoVenditeRepository,
                               ConfigurazioneRepository configurazioneRepository) {
        this.propostaRepository = propostaRepository;
        this.clienteRepository = clienteRepository;
        this.addettoVenditeRepository = addettoVenditeRepository;
        this.configurazioneRepository = configurazioneRepository;
    }

    @Override
    public List<PropostaDTO> findAll() {
        return propostaRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public PropostaDTO findById(Long id) {
        Proposta proposta = propostaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Proposta non trovata con id: " + id));
        return toDto(proposta);
    }

    @Override
    public List<PropostaDTO> findByClienteId(Long clienteId) {
        return propostaRepository.findByCliente_Id(clienteId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PropostaDTO> findByAddettoVenditeId(Long addettoVenditeId) {
        return propostaRepository.findByAddettoVendite_Id(addettoVenditeId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public PropostaDTO create(PropostaDTO dto) {
        Proposta proposta = new Proposta();

        // --- TC4_02: Cliente non trovato ---
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente non trovato"));

        // --- TC4_05: Operatore non autorizzato (se specificato) ---
        if (dto.getAddettoVenditeId() != null) {
            AddettoVendite addetto = addettoVenditeRepository.findById(dto.getAddettoVenditeId())
                    .orElseThrow(() -> new IllegalStateException("Operatore non autorizzato"));

            if (!addetto.isAttivo()) {
                throw new IllegalStateException("Operatore non autorizzato");
            }

            proposta.setAddettoVendite(addetto);
        } else {
            // creata dal cliente → nessun addetto ancora assegnato
            proposta.setAddettoVendite(null);
        }

        // --- TC4_03: Configurazione non valida ---
        Configurazione configurazione = configurazioneRepository.findById(dto.getConfigurazioneId())
                .orElseThrow(() -> new IllegalArgumentException("Configurazione non valida"));

        // la configurazione deve appartenere allo stesso cliente
        if (configurazione.getCliente() == null ||
                !configurazione.getCliente().getId().equals(cliente.getId())) {
            throw new IllegalArgumentException("Configurazione non valida");
        }

        // --- TC4_04: Veicolo non disponibile ---
        Veicolo veicolo = configurazione.getVeicolo();
        if (veicolo == null ||
                veicolo.getStato() != StatoVeicolo.DISPONIBILE ||
                !veicolo.isVisibileAlPubblico()) {
            throw new IllegalStateException("Veicolo non disponibile");
        }

        // se tutti i controlli sono passati, setto cliente+configurazione
        proposta.setCliente(cliente);
        proposta.setConfigurazione(configurazione);

        proposta.setPrezzoProposta(dto.getPrezzoProposta());
        proposta.setStato(dto.getStato());

        // se non arriva la data, imposto oggi
        proposta.setDataCreazione(
                dto.getDataCreazione() != null ? dto.getDataCreazione() : LocalDateTime.now()
        );
        proposta.setDataScadenza(dto.getDataScadenza());
        proposta.setNoteCliente(dto.getNoteCliente());
        proposta.setNoteInterne(dto.getNoteInterne());

        proposta = propostaRepository.save(proposta);
        return toDto(proposta);
    }

    @Override
    public PropostaDTO update(Long id, PropostaDTO dto) {
        Proposta proposta = propostaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Proposta non trovata con id: " + id));

        if (dto.getClienteId() != null &&
                (proposta.getCliente() == null || !proposta.getCliente().getId().equals(dto.getClienteId()))) {
            Cliente cliente = clienteRepository.findById(dto.getClienteId())
                    .orElseThrow(() -> new EntityNotFoundException("Cliente non trovato con id: " + dto.getClienteId()));
            proposta.setCliente(cliente);
        }

        if (dto.getAddettoVenditeId() != null &&
                (proposta.getAddettoVendite() == null || !proposta.getAddettoVendite().getId().equals(dto.getAddettoVenditeId()))) {
            AddettoVendite addetto = addettoVenditeRepository.findById(dto.getAddettoVenditeId())
                    .orElseThrow(() -> new EntityNotFoundException("Addetto vendite non trovato con id: " + dto.getAddettoVenditeId()));
            proposta.setAddettoVendite(addetto);
        }

        if (dto.getConfigurazioneId() != null &&
                (proposta.getConfigurazione() == null || !proposta.getConfigurazione().getId().equals(dto.getConfigurazioneId()))) {
            Configurazione configurazione = configurazioneRepository.findById(dto.getConfigurazioneId())
                    .orElseThrow(() -> new EntityNotFoundException("Configurazione non trovata con id: " + dto.getConfigurazioneId()));
            proposta.setConfigurazione(configurazione);
        }

        proposta.setPrezzoProposta(dto.getPrezzoProposta());
        proposta.setStato(dto.getStato());
        proposta.setDataCreazione(dto.getDataCreazione());
        proposta.setDataScadenza(dto.getDataScadenza());
        proposta.setNoteCliente(dto.getNoteCliente());
        proposta.setNoteInterne(dto.getNoteInterne());

        proposta = propostaRepository.save(proposta);
        return toDto(proposta);
    }

    @Override
    public void delete(Long id) {
        Proposta proposta = propostaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Proposta non trovata con id: " + id));
        propostaRepository.delete(proposta);
    }

    // -------- mapping helper --------

    private PropostaDTO toDto(Proposta entity) {
        PropostaDTO dto = new PropostaDTO();
        dto.setId(entity.getId());
        dto.setClienteId(entity.getCliente() != null ? entity.getCliente().getId() : null);
        dto.setAddettoVenditeId(entity.getAddettoVendite() != null ? entity.getAddettoVendite().getId() : null);
        dto.setConfigurazioneId(entity.getConfigurazione() != null ? entity.getConfigurazione().getId() : null);
        dto.setPrezzoProposta(entity.getPrezzoProposta());
        dto.setStato(entity.getStato());
        dto.setDataCreazione(entity.getDataCreazione());
        dto.setDataScadenza(entity.getDataScadenza());
        dto.setNoteCliente(entity.getNoteCliente());
        dto.setNoteInterne(entity.getNoteInterne());
        return dto;
    }
}