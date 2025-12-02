package it.autoflow.proposal.entity;

import it.autoflow.configuration.entity.Configurazione;
import it.autoflow.proposal.entity.StatoProposta;
import it.autoflow.user.entity.AddettoVendite;
import it.autoflow.user.entity.Cliente;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "proposte")
@Getter
@Setter
public class Proposta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    // 👇 può essere null
    @ManyToOne(optional = true)
    @JoinColumn(name = "addetto_vendite_id", nullable = true)
    private AddettoVendite addettoVendite;

    @ManyToOne(optional = false)
    @JoinColumn(name = "configurazione_id", nullable = false)
    private Configurazione configurazione;

    @Column(name = "prezzo_proposta", nullable = false)
    private Double prezzoProposta;

    @Column(name = "note_cliente")
    private String noteCliente;

    @Column(name = "note_interne")
    private String noteInterne;

    @Enumerated(EnumType.STRING)
    @Column(name = "stato", nullable = false)
    private StatoProposta stato;

    @Column(name = "data_creazione", nullable = false)
    private LocalDateTime dataCreazione;

    @Column(name = "data_scadenza")
    private LocalDateTime dataScadenza;
}