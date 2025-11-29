package it.autoflow.showroom.service;

import it.autoflow.showroom.dto.ShowroomFiltroDTO;
import it.autoflow.vehicle.entity.Veicolo;

import java.util.List;

public interface ShowroomService {

    List<Veicolo> search(ShowroomFiltroDTO filtro);

    Veicolo getDettaglioPubblico(Long veicoloId);
}