package it.autoflow.configuration.controller;

import it.autoflow.configuration.dto.ConfigurazioneDTO;
import it.autoflow.configuration.service.ConfigurazioneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/configurazioni")
@RequiredArgsConstructor
public class ConfigurazioneController {

    private final ConfigurazioneService configurazioneService;

    @PostMapping
    public ResponseEntity<ConfigurazioneDTO> creaConfigurazione(@RequestBody @Valid ConfigurazioneDTO dto) {
        ConfigurazioneDTO created = configurazioneService.creaConfigurazione(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConfigurazioneDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(configurazioneService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ConfigurazioneDTO>> getAll() {
        return ResponseEntity.ok(configurazioneService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConfigurazioneDTO> update(@PathVariable Long id,
                                                    @RequestBody @Valid ConfigurazioneDTO dto) {
        return ResponseEntity.ok(configurazioneService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        configurazioneService.delete(id);
        return ResponseEntity.noContent().build();
    }
}