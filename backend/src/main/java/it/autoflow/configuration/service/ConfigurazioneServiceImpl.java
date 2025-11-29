package it.autoflow.configuration.service;

import it.autoflow.configuration.dto.ConfigurazioneDTO;
import it.autoflow.configuration.entity.Configurazione;
import it.autoflow.configuration.entity.OptionalAccessorio;
import it.autoflow.configuration.repository.ConfigurazioneRepository;
import it.autoflow.configuration.repository.OptionalAccessorioRepository;
import it.autoflow.user.entity.Cliente;
import it.autoflow.user.repository.ClienteRepository;
import it.autoflow.vehicle.entity.Veicolo;
import it.autoflow.vehicle.repository.VeicoloRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConfigurazioneServiceImpl implements ConfigurazioneService {

    private final ConfigurazioneRepository configurazioneRepository;
    private final OptionalAccessorioRepository optionalAccessorioRepository;
    private final ClienteRepository clienteRepository;
    private final VeicoloRepository veicoloRepository;

    @Override
    public ConfigurazioneDTO creaConfigurazione(ConfigurazioneDTO dto) {
        Configurazione configurazione = new Configurazione();

        // Cliente
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente non trovato con id: " + dto.getClienteId()));
        configurazione.setCliente(cliente);

        // Veicolo
        Veicolo veicolo = veicoloRepository.findById(dto.getVeicoloId())
                .orElseThrow(() -> new EntityNotFoundException("Veicolo non trovato con id: " + dto.getVeicoloId()));
        configurazione.setVeicolo(veicolo);

        // Prezzo base dal veicolo (Double)
        Double prezzoBase = veicolo.getPrezzoBase();
        if (prezzoBase == null) {
            prezzoBase = 0.0;
        }
        configurazione.setPrezzoBase(prezzoBase);

        // Optional
        Set<OptionalAccessorio> opzionali = new HashSet<>();
        Double totaleOptional = 0.0;

        if (dto.getOptionalIds() != null && !dto.getOptionalIds().isEmpty()) {
            opzionali = new HashSet<>(optionalAccessorioRepository.findAllById(dto.getOptionalIds()));

            for (OptionalAccessorio opt : opzionali) {
                if (opt.getPrezzo() != null) {
                    totaleOptional += opt.getPrezzo();
                }
            }

            configurazione.setOptional(opzionali);
        }

        // Prezzo totale = base + optional
        Double prezzoTotale = prezzoBase + totaleOptional;
        configurazione.setPrezzoTotale(prezzoTotale);

        // Data creazione
        configurazione.setDataCreazione(
                dto.getDataCreazione() != null ? dto.getDataCreazione() : LocalDateTime.now()
        );

        configurazione.setNote(dto.getNote());

        Configurazione saved = configurazioneRepository.save(configurazione);
        return toDto(saved);
    }

    @Override
    public ConfigurazioneDTO getById(Long id) {
        Configurazione configurazione = configurazioneRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Configurazione non trovata con id: " + id));
        return toDto(configurazione);
    }

    @Override
    public List<ConfigurazioneDTO> getAll() {
        return configurazioneRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ConfigurazioneDTO update(Long id, ConfigurazioneDTO dto) {
        Configurazione configurazione = configurazioneRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Configurazione non trovata con id: " + id));

        // Cliente
        if (dto.getClienteId() != null &&
                (configurazione.getCliente() == null ||
                        !dto.getClienteId().equals(configurazione.getCliente().getId()))) {

            Cliente cliente = clienteRepository.findById(dto.getClienteId())
                    .orElseThrow(() -> new EntityNotFoundException("Cliente non trovato con id: " + dto.getClienteId()));
            configurazione.setCliente(cliente);
        }

        // Veicolo
        if (dto.getVeicoloId() != null &&
                (configurazione.getVeicolo() == null ||
                        !dto.getVeicoloId().equals(configurazione.getVeicolo().getId()))) {

            Veicolo veicolo = veicoloRepository.findById(dto.getVeicoloId())
                    .orElseThrow(() -> new EntityNotFoundException("Veicolo non trovato con id: " + dto.getVeicoloId()));
            configurazione.setVeicolo(veicolo);
            configurazione.setPrezzoBase(veicolo.getPrezzoBase() != null ? veicolo.getPrezzoBase() : 0.0);
        }

        // Optional
        Set<OptionalAccessorio> opzionali = new HashSet<>();
        Double totaleOptional = 0.0;

        if (dto.getOptionalIds() != null) {
            opzionali = new HashSet<>(optionalAccessorioRepository.findAllById(dto.getOptionalIds()));
            for (OptionalAccessorio opt : opzionali) {
                if (opt.getPrezzo() != null) {
                    totaleOptional += opt.getPrezzo();
                }
            }
            configurazione.setOptional(opzionali);
        }

        // Prezzo base (se non è già settato, prendo da veicolo)
        Double prezzoBase = configurazione.getPrezzoBase();
        if (prezzoBase == null) {
            if (configurazione.getVeicolo() != null && configurazione.getVeicolo().getPrezzoBase() != null) {
                prezzoBase = configurazione.getVeicolo().getPrezzoBase();
            } else {
                prezzoBase = 0.0;
            }
            configurazione.setPrezzoBase(prezzoBase);
        }

        // Prezzo totale
        Double prezzoTotale = prezzoBase + totaleOptional;
        configurazione.setPrezzoTotale(prezzoTotale);

        // Data + note
        if (dto.getDataCreazione() != null) {
            configurazione.setDataCreazione(dto.getDataCreazione());
        }
        configurazione.setNote(dto.getNote());

        Configurazione updated = configurazioneRepository.save(configurazione);
        return toDto(updated);
    }

    @Override
    public void delete(Long id) {
        if (!configurazioneRepository.existsById(id)) {
            throw new EntityNotFoundException("Configurazione non trovata con id: " + id);
        }
        configurazioneRepository.deleteById(id);
    }

    // -------------------- MAPPING --------------------

    private ConfigurazioneDTO toDto(Configurazione entity) {
        ConfigurazioneDTO dto = new ConfigurazioneDTO();

        dto.setId(entity.getId());
        dto.setClienteId(entity.getCliente() != null ? entity.getCliente().getId() : null);
        dto.setVeicoloId(entity.getVeicolo() != null ? entity.getVeicolo().getId() : null);
        dto.setPrezzoBase(entity.getPrezzoBase());
        dto.setPrezzoTotale(entity.getPrezzoTotale());
        dto.setDataCreazione(entity.getDataCreazione());
        dto.setNote(entity.getNote());

        if (entity.getOptional() != null) {
            dto.setOptionalIds(
                    entity.getOptional().stream()
                            .map(OptionalAccessorio::getId)
                            .collect(Collectors.toSet())
            );
        }

        return dto;
    }
}