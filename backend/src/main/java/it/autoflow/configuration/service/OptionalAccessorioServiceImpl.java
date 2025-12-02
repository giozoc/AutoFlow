package it.autoflow.configuration.service;

import it.autoflow.configuration.dto.OptionalAccessorioDTO;
import it.autoflow.configuration.entity.OptionalAccessorio;
import it.autoflow.configuration.repository.OptionalAccessorioRepository;
import it.autoflow.configuration.service.OptionalAccessorioService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OptionalAccessorioServiceImpl implements OptionalAccessorioService {

    private final OptionalAccessorioRepository optionalAccessorioRepository;

    public OptionalAccessorioServiceImpl(OptionalAccessorioRepository optionalAccessorioRepository) {
        this.optionalAccessorioRepository = optionalAccessorioRepository;
    }

    @Override
    public List<OptionalAccessorioDTO> findAll() {
        return optionalAccessorioRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public OptionalAccessorioDTO findById(Long id) {
        OptionalAccessorio entity = optionalAccessorioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("OptionalAccessorio non trovato con id: " + id));
        return toDto(entity);
    }

    @Override
    public OptionalAccessorioDTO findByCodice(String codice) {
        OptionalAccessorio entity = optionalAccessorioRepository.findByCodice(codice)
                .orElseThrow(() -> new EntityNotFoundException("OptionalAccessorio non trovato con codice: " + codice));
        return toDto(entity);
    }

    @Override
    public OptionalAccessorioDTO create(OptionalAccessorioDTO dto) {

        // controllo unicità codice (visto che è unique sul DB)
        optionalAccessorioRepository.findByCodice(dto.getCodice()).ifPresent(e -> {
            throw new IllegalArgumentException("Esiste già un optional con codice: " + dto.getCodice());
        });

        OptionalAccessorio entity = toEntity(dto);
        entity.setId(null); // generato dal DB

        entity = optionalAccessorioRepository.save(entity);
        return toDto(entity);
    }

    @Override
    public OptionalAccessorioDTO update(Long id, OptionalAccessorioDTO dto) {

        OptionalAccessorio esistente = optionalAccessorioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("OptionalAccessorio non trovato con id: " + id));

        // se il codice cambia, ricontrollo unicità
        if (!esistente.getCodice().equals(dto.getCodice())) {
            optionalAccessorioRepository.findByCodice(dto.getCodice()).ifPresent(e -> {
                throw new IllegalArgumentException("Esiste già un optional con codice: " + dto.getCodice());
            });
        }

        esistente.setCodice(dto.getCodice());
        esistente.setNome(dto.getNome());
        esistente.setDescrizione(dto.getDescrizione());
        esistente.setPrezzo(dto.getPrezzo());

        esistente = optionalAccessorioRepository.save(esistente);
        return toDto(esistente);
    }

    @Override
    public void delete(Long id) {
        OptionalAccessorio esistente = optionalAccessorioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("OptionalAccessorio non trovato con id: " + id));
        optionalAccessorioRepository.delete(esistente);
    }

    // --------- mapping helper ---------

    private OptionalAccessorioDTO toDto(OptionalAccessorio entity) {
        OptionalAccessorioDTO dto = new OptionalAccessorioDTO();
        dto.setId(entity.getId());
        dto.setCodice(entity.getCodice());
        dto.setNome(entity.getNome());
        dto.setDescrizione(entity.getDescrizione());
        dto.setPrezzo(entity.getPrezzo());
        return dto;
    }

    private OptionalAccessorio toEntity(OptionalAccessorioDTO dto) {
        OptionalAccessorio entity = new OptionalAccessorio();
        entity.setId(dto.getId());
        entity.setCodice(dto.getCodice());
        entity.setNome(dto.getNome());
        entity.setDescrizione(dto.getDescrizione());
        entity.setPrezzo(dto.getPrezzo());
        return entity;
    }
}