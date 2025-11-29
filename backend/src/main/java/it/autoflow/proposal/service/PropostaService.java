package it.autoflow.proposal.service;

import it.autoflow.proposal.dto.PropostaDTO;

import java.util.List;

public interface PropostaService {

    List<PropostaDTO> findAll();

    PropostaDTO findById(Long id);

    List<PropostaDTO> findByClienteId(Long clienteId);

    List<PropostaDTO> findByAddettoVenditeId(Long addettoVenditeId);

    PropostaDTO create(PropostaDTO dto);

    PropostaDTO update(Long id, PropostaDTO dto);

    void delete(Long id);
}