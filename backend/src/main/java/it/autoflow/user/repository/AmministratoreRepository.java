package it.autoflow.user.repository;

import it.autoflow.user.entity.Amministratore;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AmministratoreRepository extends JpaRepository<Amministratore, Long> {
}