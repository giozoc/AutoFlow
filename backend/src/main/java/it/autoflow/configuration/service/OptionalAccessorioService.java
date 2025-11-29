package it.autoflow.configuration.service;

import it.autoflow.configuration.dto.OptionalAccessorioDTO;

import java.util.List;

public interface OptionalAccessorioService {

    List<OptionalAccessorioDTO> findAll();

    OptionalAccessorioDTO findById(Long id);

    OptionalAccessorioDTO findByCodice(String codice);

    OptionalAccessorioDTO create(OptionalAccessorioDTO dto);

    OptionalAccessorioDTO update(Long id, OptionalAccessorioDTO dto);

    void delete(Long id);
}