package it.autoflow.configuration.repository;

import it.autoflow.configuration.entity.OptionalAccessorio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OptionalAccessorioRepository extends JpaRepository<OptionalAccessorio, Long> {

    Optional<OptionalAccessorio> findByCodice(String codice);
}