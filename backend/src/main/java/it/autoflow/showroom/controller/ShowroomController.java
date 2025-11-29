package it.autoflow.showroom.controller;

import it.autoflow.showroom.dto.ShowroomFiltroDTO;
import it.autoflow.showroom.service.ShowroomService;
import it.autoflow.vehicle.entity.Veicolo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/showroom")
@RequiredArgsConstructor
public class ShowroomController {

    private final ShowroomService showroomService;

    @PostMapping("/search")
    public List<Veicolo> search(@RequestBody ShowroomFiltroDTO filtro) {
        return showroomService.search(filtro);
    }

    @GetMapping("/{id}")
    public Veicolo dettaglio(@PathVariable Long id) {
        return showroomService.getDettaglioPubblico(id);
    }
}