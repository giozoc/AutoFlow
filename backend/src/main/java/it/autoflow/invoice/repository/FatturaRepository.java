package it.autoflow.invoice.repository;

import it.autoflow.invoice.entity.Fattura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FatturaRepository extends JpaRepository<Fattura, Long> {

    Optional<Fattura> findByNumeroFattura(String numeroFattura);

    List<Fattura> findByCliente_Id(Long clienteId);

    @Query("select coalesce(sum(f.importoTotale), 0) from Fattura f")
    Double sumImportoTotale();

    @Query("""
           select coalesce(sum(f.importoTotale), 0)
           from Fattura f
           where year(f.dataEmissione) = :anno
           """)
    Double sumImportoTotaleByAnno(@Param("anno") int anno);

    @Query("""
           select coalesce(sum(f.importoTotale), 0)
           from Fattura f
           where year(f.dataEmissione) = :anno
             and month(f.dataEmissione) = :mese
           """)
    Double sumImportoTotaleByAnnoAndMese(@Param("anno") int anno,
                                         @Param("mese") int mese);

    long countByDataPagamentoIsNull();

    Fattura findTopByNumeroFatturaStartingWithOrderByNumeroFatturaDesc(String prefix);
}