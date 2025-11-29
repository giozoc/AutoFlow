package it.autoflow.configuration.controller;

import it.autoflow.configuration.dto.OptionalAccessorioDTO;
import it.autoflow.configuration.service.OptionalAccessorioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/optional")
public class OptionalAccessorioController {

    private final OptionalAccessorioService optionalAccessorioService;

    public OptionalAccessorioController(OptionalAccessorioService optionalAccessorioService) {
        this.optionalAccessorioService = optionalAccessorioService;
    }

    @GetMapping
    public List<OptionalAccessorioDTO> getAll() {
        return optionalAccessorioService.findAll();
    }

    @GetMapping("/{id}")
    public OptionalAccessorioDTO getById(@PathVariable Long id) {
        return optionalAccessorioService.findById(id);
    }

    @GetMapping("/codice/{codice}")
    public OptionalAccessorioDTO getByCodice(@PathVariable String codice) {
        return optionalAccessorioService.findByCodice(codice);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OptionalAccessorioDTO create(@Valid @RequestBody OptionalAccessorioDTO dto) {
        return optionalAccessorioService.create(dto);
    }

    @PutMapping("/{id}")
    public OptionalAccessorioDTO update(@PathVariable Long id,
                                        @Valid @RequestBody OptionalAccessorioDTO dto) {
        return optionalAccessorioService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        optionalAccessorioService.delete(id);
    }
}