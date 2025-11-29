package it.autoflow.vehicle.service;

import it.autoflow.commons.service.CrudService;
import it.autoflow.vehicle.entity.StatoVeicolo;
import it.autoflow.vehicle.entity.Veicolo;

public interface VeicoloService extends CrudService<Veicolo, Long> {

    Veicolo changeState(Long veicoloId, StatoVeicolo nuovoStato);

    Veicolo duplicate(Long veicoloId);

    boolean checkUnicitaTargaVin(String targa, String vin);
}