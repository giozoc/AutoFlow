package it.autoflow.vehicle.repository;

import it.autoflow.vehicle.entity.Veicolo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VeicoloRepository extends JpaRepository<Veicolo, Long> {

    Optional<Veicolo> findByTarga(String targa);

    Optional<Veicolo> findByVin(String vin);
}