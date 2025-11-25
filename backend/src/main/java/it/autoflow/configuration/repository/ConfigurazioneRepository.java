package it.autoflow.configuration.repository;

import it.autoflow.configuration.entity.Configurazione;
import it.autoflow.user.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConfigurazioneRepository extends JpaRepository<Configurazione, Long> {

    List<Configurazione> findByCliente(Cliente cliente);
}