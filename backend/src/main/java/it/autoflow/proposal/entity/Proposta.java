package it.autoflow.proposal.entity;

import it.autoflow.configuration.entity.Configurazione;
import it.autoflow.user.entity.AddettoVendite;
import it.autoflow.user.entity.Cliente;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDate;

@Getter @Setter
@Entity
@Table(name = "proposte")
public class Proposta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Cliente cliente;

    @ManyToOne(optional = false)
    private AddettoVendite addettoVendite;

    @OneToOne(optional = false)
    private Configurazione configurazione;

    @Column(nullable = false)
    private Double prezzoProposta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoProposta stato = StatoProposta.BOZZA;

    @Column(nullable = false)
    private LocalDate dataCreazione;

    private LocalDate dataScadenza;

    private String noteCliente;
    private String noteInterne;
}