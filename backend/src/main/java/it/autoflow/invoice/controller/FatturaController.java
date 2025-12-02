package it.autoflow.invoice.controller;

import it.autoflow.commons.entity.DocumentoPDF;
import it.autoflow.commons.repository.DocumentoPDFRepository;
import it.autoflow.commons.service.FileStorageService;
import it.autoflow.commons.service.PdfDocumentService;
import it.autoflow.invoice.dto.FatturaDTO;
import it.autoflow.invoice.service.FatturaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@RestController
@RequestMapping("/api/fatture")
@RequiredArgsConstructor
public class FatturaController {

    private final FatturaService fatturaService;
    private final PdfDocumentService pdfDocumentService;
    private final FileStorageService fileStorageService;
    private final DocumentoPDFRepository documentoPDFRepository;

    @GetMapping
    public List<FatturaDTO> findAll() {
        return fatturaService.findAll();
    }


    @GetMapping("/cliente/{id}")
    public List<FatturaDTO> getByCliente(@PathVariable("id") Long clienteId) {
        return fatturaService.findByClienteId(clienteId);
    }

    @GetMapping("/{id}")
    public FatturaDTO findById(@PathVariable Long id) {
        return fatturaService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FatturaDTO create(@Valid @RequestBody FatturaDTO dto) {
        return fatturaService.create(dto);
    }

    @PutMapping("/{id}")
    public FatturaDTO update(@PathVariable Long id,
                             @Valid @RequestBody FatturaDTO dto) {
        return fatturaService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        fatturaService.delete(id);
    }

    // ---------------------------------------------------------
    //  GET /api/fatture/{id}/pdf  → application/pdf
    // ---------------------------------------------------------
    @GetMapping("/{id}/pdf")
    public ResponseEntity<Resource> downloadPdf(@PathVariable Long id) {
        // 1) leggo la fattura come DTO
        FatturaDTO fattura = fatturaService.findById(id);

        Long documentoId = fattura.getDocumentoPdfId();

        // 2) se non esiste ancora il PDF, lo genero adesso
        if (documentoId == null) {
            DocumentoPDF documento = pdfDocumentService.generateInvoicePdf(id);
            documentoId = documento.getId();
        }

        DocumentoPDF documento = documentoPDFRepository.findById(documentoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "DocumentoPDF non trovato per fattura " + id
                ));

        File file = fileStorageService.load(documento.getPath());
        if (!file.exists()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "File PDF non trovato su disco"
            );
        }

        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            ByteArrayResource resource = new ByteArrayResource(bytes);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + documento.getNomeFile() + "\"")
                    .body(resource);
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Errore nella lettura del PDF",
                    e
            );
        }
    }

    @PostMapping("/da-proposta/{propostaId}")
    @ResponseStatus(HttpStatus.CREATED)
    public FatturaDTO createFromProposta(@PathVariable Long propostaId) {
        return fatturaService.createFromProposta(propostaId);
    }
}