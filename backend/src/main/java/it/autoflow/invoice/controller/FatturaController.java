package it.autoflow.invoice.controller;

import it.autoflow.invoice.dto.FatturaDTO;
import it.autoflow.invoice.service.FatturaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fatture")
public class FatturaController {

    private final FatturaService fatturaService;

    public FatturaController(FatturaService fatturaService) {
        this.fatturaService = fatturaService;
    }

    @GetMapping
    public List<FatturaDTO> getAll() {
        return fatturaService.findAll();
    }

    @GetMapping("/{id}")
    public FatturaDTO getById(@PathVariable Long id) {
        return fatturaService.findById(id);
    }

    @GetMapping("/numero/{numeroFattura}")
    public FatturaDTO getByNumeroFattura(@PathVariable String numeroFattura) {
        return fatturaService.findByNumeroFattura(numeroFattura);
    }

    @GetMapping("/cliente/{clienteId}")
    public List<FatturaDTO> getByCliente(@PathVariable Long clienteId) {
        return fatturaService.findByClienteId(clienteId);
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
}