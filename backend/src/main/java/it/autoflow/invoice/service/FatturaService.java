package it.autoflow.invoice.service;

import it.autoflow.invoice.dto.FatturaDTO;

import java.util.List;

public interface FatturaService {

    List<FatturaDTO> findAll();

    FatturaDTO findById(Long id);

    FatturaDTO findByNumeroFattura(String numeroFattura);

    List<FatturaDTO> findByClienteId(Long clienteId);

    FatturaDTO create(FatturaDTO dto);

    FatturaDTO update(Long id, FatturaDTO dto);

    void delete(Long id);

    FatturaDTO createFromProposta(Long propostaId);
}