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

        // 1️⃣ prendiamo SOLO veicoli visibili + DISPONIBILE
        List<Veicolo> base = veicoloRepository
                .findByVisibileAlPubblicoTrueAndStato(StatoVeicolo.DISPONIBILE);

        // 2️⃣ applichiamo i filtri, tenendo conto di NULL **e** stringa vuota
        return base.stream()
                .filter(v ->
                        filtro.getMarca() == null
                                || filtro.getMarca().isBlank()
                                || v.getMarca().equalsIgnoreCase(filtro.getMarca())
                )
                .filter(v ->
                        filtro.getModello() == null
                                || filtro.getModello().isBlank()
                                || v.getModello().equalsIgnoreCase(filtro.getModello())
                )
                // 👇 qui dipende dal tipo di prezzoBase / prezzoMin/Max:
                // se usi BigDecimal, usa compareTo come sotto.
                // se usi double/Double puoi lasciare >= e <=
                .filter(v ->
                        filtro.getPrezzoMin() == null
                                || v.getPrezzoBase().compareTo(filtro.getPrezzoMin()) >= 0
                )
                .filter(v ->
                        filtro.getPrezzoMax() == null
                                || v.getPrezzoBase().compareTo(filtro.getPrezzoMax()) <= 0
                )
                .collect(Collectors.toList());
    }

    @Override
    public Veicolo getDettaglioPubblico(Long veicoloId) {
        return veicoloRepository.findById(veicoloId)
                // mostriamo il dettaglio solo se è ancora visibile + disponibile
                .filter(v -> v.isVisibileAlPubblico() && v.getStato() == StatoVeicolo.DISPONIBILE)
                .orElse(null);
    }
}