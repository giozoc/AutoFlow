package it.autoflow.showroom.service;

import it.autoflow.showroom.dto.ShowroomFiltroDTO;
import it.autoflow.vehicle.entity.StatoVeicolo;
import it.autoflow.vehicle.entity.Veicolo;
import it.autoflow.vehicle.repository.VeicoloRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShowroomServiceImpl implements ShowroomService {

    private final VeicoloRepository veicoloRepository;

    @Override
    public List<Veicolo> search(ShowroomFiltroDTO filtro) {
        return veicoloRepository.findAll().stream()
                .filter(v -> v.getStato() == StatoVeicolo.DISPONIBILE)
                .filter(v -> filtro.getMarca() == null || v.getMarca().equalsIgnoreCase(filtro.getMarca()))
                .filter(v -> filtro.getModello() == null || v.getModello().equalsIgnoreCase(filtro.getModello()))
                .filter(v -> filtro.getPrezzoMin() == null || v.getPrezzoBase() >= filtro.getPrezzoMin())
                .filter(v -> filtro.getPrezzoMax() == null || v.getPrezzoBase() <= filtro.getPrezzoMax())
                .collect(Collectors.toList());
    }

    @Override
    public Veicolo getDettaglioPubblico(Long veicoloId) {
        Veicolo v = veicoloRepository.findById(veicoloId).orElse(null);
        if (v != null && v.getStato() == StatoVeicolo.DISPONIBILE)
            return v;
        return null;
    }
}