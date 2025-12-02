package it.autoflow.vehicle.service;

import it.autoflow.vehicle.dto.VeicoloDTO;
import it.autoflow.vehicle.entity.StatoVeicolo;
import it.autoflow.vehicle.entity.Veicolo;
import it.autoflow.vehicle.repository.VeicoloRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VeicoloServiceImpl implements VeicoloService {

    private final VeicoloRepository veicoloRepository;

    @Override
    public Veicolo create(Veicolo entity) {
        return veicoloRepository.save(entity);
    }

    @Override
    public Veicolo getById(Long id) {
        return veicoloRepository.findById(id).orElse(null);
    }

    @Override
    public Veicolo update(Long id, Veicolo entity) {
        entity.setId(id);
        return veicoloRepository.save(entity);
    }

    @Override
    public boolean delete(Long id) {
        if (!veicoloRepository.existsById(id)) return false;
        veicoloRepository.deleteById(id);
        return true;
    }

    @Override
    public List<Veicolo> findAll() {
        return veicoloRepository.findAll();
    }

    @Override
    public Veicolo changeState(Long veicoloId, StatoVeicolo nuovoStato) {
        Veicolo v = getById(veicoloId);
        if (v == null) return null;
        v.setStato(nuovoStato);
        return veicoloRepository.save(v);
    }


    @Override
    public Veicolo duplicate(Long veicoloId) {
        Veicolo originale = getById(veicoloId);
        if (originale == null) return null;

        Veicolo nuovo = new Veicolo();
        nuovo.setMarca(originale.getMarca());
        nuovo.setModello(originale.getModello());
        nuovo.setAnno(originale.getAnno());
        nuovo.setPrezzoBase(originale.getPrezzoBase());
        nuovo.setChilometraggio(originale.getChilometraggio());
        nuovo.setAlimentazione(originale.getAlimentazione());
        nuovo.setCambio(originale.getCambio());
        nuovo.setColoreEsterno(originale.getColoreEsterno());
        nuovo.setStato(StatoVeicolo.DISPONIBILE);
        nuovo.setVisibileAlPubblico(originale.isVisibileAlPubblico());

        // NON duplicare targa e VIN
        nuovo.setTarga(null);
        nuovo.setVin(null);

        return veicoloRepository.save(nuovo);
    }

    @Override
    public boolean checkUnicitaTargaVin(String targa, String vin) {
        return !veicoloRepository.existsByTargaOrVin(targa, vin);
    }

    @Override
    public List<VeicoloDTO> listShowroom() {
        return veicoloRepository
                .findByVisibileAlPubblicoTrueAndStato(StatoVeicolo.DISPONIBILE)
                .stream()
                .map(this::toDto)
                .toList();
    }


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
}