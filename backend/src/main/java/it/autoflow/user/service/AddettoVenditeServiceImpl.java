package it.autoflow.user.service;

import it.autoflow.authentication.utils.PasswordHasher;
import it.autoflow.commons.service.CrudService;
import it.autoflow.user.entity.AddettoVendite;
import it.autoflow.user.entity.Ruolo;
import it.autoflow.user.repository.AddettoVenditeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddettoVenditeServiceImpl implements AddettoVenditeService, CrudService<AddettoVendite, Long> {

    private final AddettoVenditeRepository addettoVenditeRepository;

    @Override
    public AddettoVendite create(AddettoVendite entity) {

        // username = email per assicurare lo stesso login degli altri utenti
        entity.setUsername(entity.getEmail());

        // Se non c’è password → generiamo una password temporanea
        if (entity.getPassword() == null || entity.getPassword().isBlank()) {
            String tempPassword = UUID.randomUUID().toString().substring(0, 8);
            entity.setPassword(PasswordHasher.sha512(tempPassword));
            // TODO: inviare via email la password temporanea
            System.out.println("Password temporanea addetto vendite: " + tempPassword);
        } else {
            // Se l’admin la sta impostando manualmente → hashiamo
            entity.setPassword(PasswordHasher.sha512(entity.getPassword()));
        }

        // Ruolo obbligatorio: addetto vendite
        if (entity.getRuolo() == null) {
            entity.setRuolo(Ruolo.ADDETTO_VENDITE);
        }

        // Attivo di default
        entity.setAttivo(true);

        return addettoVenditeRepository.save(entity);
    }

    @Override
    public AddettoVendite getById(Long id) {
        return addettoVenditeRepository.findById(id).orElse(null);
    }

    @Override
    public AddettoVendite update(Long id, AddettoVendite entity) {
        entity.setId(id);
        return addettoVenditeRepository.save(entity);
    }

    @Override
    public boolean delete(Long id) {
        if (!addettoVenditeRepository.existsById(id)) {
            return false;
        }
        addettoVenditeRepository.deleteById(id);
        return true;
    }

    @Override
    public List<AddettoVendite> findAll() {
        return addettoVenditeRepository.findAll();
    }

    @Override
    public boolean resetPassword(Long addettoId) {
        return addettoVenditeRepository.findById(addettoId).map(addetto -> {
            String tempPassword = UUID.randomUUID().toString().substring(0, 8);
            addetto.setPassword(PasswordHasher.sha512(tempPassword));
            addettoVenditeRepository.save(addetto);
            // TODO: comunicare la nuova password all'addetto (email, ecc.)
            System.out.println("Password temporanea addetto vendite: " + tempPassword);
            return true;
        }).orElse(false);
    }

    @Override
    public AddettoVendite deactivateAddetto(Long addettoId) {
        return addettoVenditeRepository.findById(addettoId).map(addetto -> {
            addetto.setAttivo(!addetto.isAttivo());
            return addettoVenditeRepository.save(addetto);
        }).orElse(null);
    }
}