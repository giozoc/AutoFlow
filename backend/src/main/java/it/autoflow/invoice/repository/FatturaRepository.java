package it.autoflow.invoice.repository;

import it.autoflow.invoice.entity.Fattura;
import it.autoflow.user.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FatturaRepository extends JpaRepository<Fattura, Long> {

    Optional<Fattura> findByNumeroFattura(String numeroFattura);

    List<Fattura> findByCliente(Cliente cliente);
}