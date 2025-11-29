package it.autoflow.user.service;

import it.autoflow.commons.service.CrudService;
import it.autoflow.user.entity.Amministratore;
import it.autoflow.user.repository.AmministratoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AmministratoreServiceImpl implements AmministratoreService, CrudService<Amministratore, Long> {

    private final AmministratoreRepository amministratoreRepository;

    @Override
    public Amministratore create(Amministratore entity) {
        return amministratoreRepository.save(entity);
    }

    @Override
    public Amministratore getById(Long id) {
        return amministratoreRepository.findById(id).orElse(null);
    }

    @Override
    public Amministratore update(Long id, Amministratore entity) {
        entity.setId(id);
        return amministratoreRepository.save(entity);
    }

    @Override
    public boolean delete(Long id) {
        if (!amministratoreRepository.existsById(id)) {
            return false;
        }
        amministratoreRepository.deleteById(id);
        return true;
    }

    @Override
    public List<Amministratore> findAll() {
        return amministratoreRepository.findAll();
    }
}