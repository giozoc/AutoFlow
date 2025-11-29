package it.autoflow.proposal.controller;

import it.autoflow.proposal.dto.PropostaDTO;
import it.autoflow.proposal.service.PropostaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proposte")
public class PropostaController {

    private final PropostaService propostaService;

    public PropostaController(PropostaService propostaService) {
        this.propostaService = propostaService;
    }

    @GetMapping
    public List<PropostaDTO> getAll() {
        return propostaService.findAll();
    }

    @GetMapping("/{id}")
    public PropostaDTO getById(@PathVariable Long id) {
        return propostaService.findById(id);
    }

    @GetMapping("/cliente/{clienteId}")
    public List<PropostaDTO> getByCliente(@PathVariable Long clienteId) {
        return propostaService.findByClienteId(clienteId);
    }

    @GetMapping("/addetto/{addettoId}")
    public List<PropostaDTO> getByAddettoVendite(@PathVariable("addettoId") Long addettoId) {
        return propostaService.findByAddettoVenditeId(addettoId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PropostaDTO create(@Valid @RequestBody PropostaDTO dto) {
        return propostaService.create(dto);
    }

    @PutMapping("/{id}")
    public PropostaDTO update(@PathVariable Long id,
                              @Valid @RequestBody PropostaDTO dto) {
        return propostaService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        propostaService.delete(id);
    }
}