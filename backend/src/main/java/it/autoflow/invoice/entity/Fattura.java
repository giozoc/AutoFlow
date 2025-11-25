package it.autoflow.invoice.entity;

import it.autoflow.commons.entity.DocumentoPDF;
import it.autoflow.proposal.entity.Proposta;
import it.autoflow.user.entity.Cliente;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
@Entity
@Table(name = "fatture")
public class Fattura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numeroFattura;

    @Column(nullable = false)
    private LocalDate dataEmissione;

    @ManyToOne(optional = false)
    private Cliente cliente;

    @OneToOne(optional = false)
    private Proposta proposta;

    @Column(nullable = false)
    private BigDecimal importoTotale;

    private LocalDate dataPagamento;

    @OneToOne
    private DocumentoPDF documentoPdf;
}