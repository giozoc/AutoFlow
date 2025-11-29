package it.autoflow.invoice.service;

import it.autoflow.commons.entity.DocumentoPDF;
import it.autoflow.commons.repository.DocumentoPDFRepository;
import it.autoflow.invoice.dto.FatturaDTO;
import it.autoflow.invoice.entity.Fattura;
import it.autoflow.invoice.repository.FatturaRepository;
import it.autoflow.invoice.service.FatturaService;
import it.autoflow.proposal.entity.Proposta;
import it.autoflow.proposal.repository.PropostaRepository;
import it.autoflow.user.entity.Cliente;
import it.autoflow.user.repository.ClienteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FatturaServiceImpl implements FatturaService {

    private final FatturaRepository fatturaRepository;
    private final ClienteRepository clienteRepository;
    private final PropostaRepository propostaRepository;
    private final DocumentoPDFRepository documentoPDFRepository;

    public FatturaServiceImpl(FatturaRepository fatturaRepository,
                              ClienteRepository clienteRepository,
                              PropostaRepository propostaRepository,
                              DocumentoPDFRepository documentoPDFRepository) {
        this.fatturaRepository = fatturaRepository;
        this.clienteRepository = clienteRepository;
        this.propostaRepository = propostaRepository;
        this.documentoPDFRepository = documentoPDFRepository;
    }

    @Override
    public List<FatturaDTO> findAll() {
        return fatturaRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public FatturaDTO findById(Long id) {
        Fattura fattura = fatturaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Fattura non trovata con id: " + id));
        return toDto(fattura);
    }

    @Override
    public FatturaDTO findByNumeroFattura(String numeroFattura) {
        Fattura fattura = fatturaRepository.findByNumeroFattura(numeroFattura)
                .orElseThrow(() -> new EntityNotFoundException("Fattura non trovata con numero: " + numeroFattura));
        return toDto(fattura);
    }

    @Override
    public List<FatturaDTO> findByClienteId(Long clienteId) {
        return fatturaRepository.findByCliente_Id(clienteId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public FatturaDTO create(FatturaDTO dto) {
        Fattura fattura = new Fattura();

        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente non trovato con id: " + dto.getClienteId()));

        Proposta proposta = propostaRepository.findById(dto.getPropostaId())
                .orElseThrow(() -> new EntityNotFoundException("Proposta non trovata con id: " + dto.getPropostaId()));

        DocumentoPDF documentoPDF = null;
        if (dto.getDocumentoPdfId() != null) {
            documentoPDF = documentoPDFRepository.findById(dto.getDocumentoPdfId())
                    .orElseThrow(() -> new EntityNotFoundException("Documento PDF non trovato con id: " + dto.getDocumentoPdfId()));
        }

        fattura.setNumeroFattura(dto.getNumeroFattura());
        fattura.setDataEmissione(
                dto.getDataEmissione() != null ? dto.getDataEmissione() : LocalDate.now()
        );
        fattura.setCliente(cliente);
        fattura.setProposta(proposta);
        fattura.setImportoTotale(dto.getImportoTotale());
        fattura.setDataPagamento(dto.getDataPagamento());
        fattura.setDocumentoPdf(documentoPDF);

        fattura = fatturaRepository.save(fattura);
        return toDto(fattura);
    }

    @Override
    public FatturaDTO update(Long id, FatturaDTO dto) {
        Fattura fattura = fatturaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Fattura non trovata con id: " + id));

        if (dto.getClienteId() != null &&
                (fattura.getCliente() == null || !fattura.getCliente().getId().equals(dto.getClienteId()))) {
            Cliente cliente = clienteRepository.findById(dto.getClienteId())
                    .orElseThrow(() -> new EntityNotFoundException("Cliente non trovato con id: " + dto.getClienteId()));
            fattura.setCliente(cliente);
        }

        if (dto.getPropostaId() != null &&
                (fattura.getProposta() == null || !fattura.getProposta().getId().equals(dto.getPropostaId()))) {
            Proposta proposta = propostaRepository.findById(dto.getPropostaId())
                    .orElseThrow(() -> new EntityNotFoundException("Proposta non trovata con id: " + dto.getPropostaId()));
            fattura.setProposta(proposta);
        }

        if (dto.getDocumentoPdfId() != null) {
            DocumentoPDF documentoPDF = documentoPDFRepository.findById(dto.getDocumentoPdfId())
                    .orElseThrow(() -> new EntityNotFoundException("Documento PDF non trovato con id: " + dto.getDocumentoPdfId()));
            fattura.setDocumentoPdf(documentoPDF);
        } else {
            fattura.setDocumentoPdf(null);
        }

        fattura.setNumeroFattura(dto.getNumeroFattura());
        fattura.setDataEmissione(dto.getDataEmissione());
        fattura.setImportoTotale(dto.getImportoTotale());
        fattura.setDataPagamento(dto.getDataPagamento());

        fattura = fatturaRepository.save(fattura);
        return toDto(fattura);
    }

    @Override
    public void delete(Long id) {
        Fattura fattura = fatturaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Fattura non trovata con id: " + id));
        fatturaRepository.delete(fattura);
    }

    // --------- mapping helper ---------

    private FatturaDTO toDto(Fattura entity) {
        FatturaDTO dto = new FatturaDTO();
        dto.setId(entity.getId());
        dto.setNumeroFattura(entity.getNumeroFattura());
        dto.setDataEmissione(entity.getDataEmissione());
        dto.setClienteId(entity.getCliente() != null ? entity.getCliente().getId() : null);
        dto.setPropostaId(entity.getProposta() != null ? entity.getProposta().getId() : null);
        dto.setImportoTotale(entity.getImportoTotale());
        dto.setDataPagamento(entity.getDataPagamento());
        dto.setDocumentoPdfId(entity.getDocumentoPdf() != null ? entity.getDocumentoPdf().getId() : null);
        return dto;
    }
}