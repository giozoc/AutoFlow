package it.autoflow.vehicle.service;

import it.autoflow.commons.service.CrudService;
import it.autoflow.vehicle.dto.VeicoloDTO;
import it.autoflow.vehicle.entity.StatoVeicolo;
import it.autoflow.vehicle.entity.Veicolo;

import java.util.List;

public interface VeicoloService extends CrudService<Veicolo, Long> {

    Veicolo changeState(Long veicoloId, StatoVeicolo nuovoStato);

    Veicolo duplicate(Long veicoloId);

    boolean checkUnicitaTargaVin(String targa, String vin);

    public List<VeicoloDTO> listShowroom();
}