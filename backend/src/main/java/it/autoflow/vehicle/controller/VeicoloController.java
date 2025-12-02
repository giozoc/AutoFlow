package it.autoflow.vehicle.controller;

import it.autoflow.vehicle.dto.VeicoloDTO;
import it.autoflow.vehicle.entity.StatoVeicolo;
import it.autoflow.vehicle.entity.Veicolo;
import it.autoflow.vehicle.service.VeicoloService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/veicoli")
@RequiredArgsConstructor
public class VeicoloController {

    private final VeicoloService veicoloService;

    @GetMapping
    public List<VeicoloDTO> getAll() {
        return veicoloService.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public VeicoloDTO getById(@PathVariable Long id) {
        Veicolo v = veicoloService.getById(id);
        return v != null ? toDto(v) : null;
    }

    @PostMapping
    public VeicoloDTO create(@RequestBody VeicoloDTO dto) {
        Veicolo saved = veicoloService.create(toEntity(dto));
        return toDto(saved);
    }

    @PutMapping("/{id}")
    public VeicoloDTO update(@PathVariable Long id, @RequestBody VeicoloDTO dto) {
        Veicolo updated = veicoloService.update(id, toEntity(dto));
        return toDto(updated);
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return veicoloService.delete(id);
    }

    @PostMapping("/{id}/stato")
    public VeicoloDTO changeState(@PathVariable Long id, @RequestParam StatoVeicolo stato) {
        Veicolo updated = veicoloService.changeState(id, stato);
        return updated != null ? toDto(updated) : null;
    }

    @PostMapping("/{id}/duplicate")
    public VeicoloDTO duplicate(@PathVariable Long id) {
        Veicolo duplicated = veicoloService.duplicate(id);
        return duplicated != null ? toDto(duplicated) : null;
    }


    // Mapper 🔽
    private VeicoloDTO toDto(Veicolo v) {
        VeicoloDTO dto = new VeicoloDTO();
        dto.setId(v.getId());
        dto.setMarca(v.getMarca());
        dto.setModello(v.getModello());
        dto.setAnno(v.getAnno());
        dto.setTarga(v.getTarga());
        dto.setVin(v.getVin());
        dto.setPrezzoBase(v.getPrezzoBase());
        dto.setChilometraggio(v.getChilometraggio());
        dto.setAlimentazione(v.getAlimentazione());
        dto.setCambio(v.getCambio());
        dto.setColoreEsterno(v.getColoreEsterno());
        dto.setStato(v.getStato());
        dto.setVisibileAlPubblico(v.isVisibileAlPubblico());
        return dto;
    }

    private Veicolo toEntity(VeicoloDTO dto) {
        Veicolo v = new Veicolo();
        v.setId(dto.getId());
        v.setMarca(dto.getMarca());
        v.setModello(dto.getModello());
        v.setAnno(dto.getAnno());
        v.setTarga(dto.getTarga());
        v.setVin(dto.getVin());
        v.setPrezzoBase(dto.getPrezzoBase());
        v.setChilometraggio(dto.getChilometraggio());
        v.setAlimentazione(dto.getAlimentazione());
        v.setCambio(dto.getCambio());
        v.setColoreEsterno(dto.getColoreEsterno());
        v.setStato(dto.getStato());
        v.setVisibileAlPubblico(dto.isVisibileAlPubblico());
        return v;
    }

    @GetMapping("/showroom")
    public List<VeicoloDTO> getShowroom() {
        return veicoloService.listShowroom();
    }
}