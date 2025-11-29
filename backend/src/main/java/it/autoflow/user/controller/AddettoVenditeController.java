package it.autoflow.user.controller;

import it.autoflow.user.dto.AddettoVenditeDTO;
import it.autoflow.user.entity.AddettoVendite;
import it.autoflow.user.entity.Ruolo;
import it.autoflow.user.service.AddettoVenditeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/staff")
@RequiredArgsConstructor
public class AddettoVenditeController {

    private final AddettoVenditeService addettoVenditeService;

    @GetMapping
    public List<AddettoVenditeDTO> getAll() {
        return addettoVenditeService.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public AddettoVenditeDTO getById(@PathVariable Long id) {
        AddettoVendite addetto = addettoVenditeService.getById(id);
        return addetto != null ? toDto(addetto) : null;
    }

    @PostMapping
    public AddettoVenditeDTO create(@RequestBody AddettoVenditeDTO dto) {
        AddettoVendite saved = addettoVenditeService.create(toEntity(dto));
        return toDto(saved);
    }

    @PutMapping("/{id}")
    public AddettoVenditeDTO update(@PathVariable Long id, @RequestBody AddettoVenditeDTO dto) {
        AddettoVendite updated = addettoVenditeService.update(id, toEntity(dto));
        return toDto(updated);
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return addettoVenditeService.delete(id);
    }

    @PostMapping("/{id}/reset-password")
    public boolean resetPassword(@PathVariable Long id) {
        return addettoVenditeService.resetPassword(id);
    }

    @PostMapping("/{id}/toggle-active")
    public AddettoVenditeDTO toggleActive(@PathVariable Long id) {
        AddettoVendite updated = addettoVenditeService.deactivateAddetto(id);
        return updated != null ? toDto(updated) : null;
    }

    // ---------- mapping helper ----------

    private AddettoVenditeDTO toDto(AddettoVendite a) {
        AddettoVenditeDTO dto = new AddettoVenditeDTO();
        dto.setId(a.getId());
        dto.setNome(a.getNome());
        dto.setCognome(a.getCognome());
        dto.setMatricola(a.getMatricola());
        dto.setEmail(a.getEmail());
        dto.setTelefono(a.getTelefono());
        dto.setAttivo(a.isAttivo());
        dto.setRuolo(a.getRuolo().name());
        return dto;
    }

    private AddettoVendite toEntity(AddettoVenditeDTO dto) {
        AddettoVendite a = new AddettoVendite();
        a.setId(dto.getId());
        a.setNome(dto.getNome());
        a.setCognome(dto.getCognome());
        a.setMatricola(dto.getMatricola());
        a.setEmail(dto.getEmail());
        a.setTelefono(dto.getTelefono());
        a.setPassword(dto.getPassword()); // la hash viene fatta nel service
        a.setAttivo(dto.isAttivo());
        if (dto.getRuolo() != null) {
            a.setRuolo(Ruolo.valueOf(dto.getRuolo()));
        }
        return a;
    }
}