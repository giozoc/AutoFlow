package it.autoflow.vehicle.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@Entity
@Table(name = "veicoli")
public class Veicolo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String marca;

    @Column(nullable = false)
    private String modello;

    private Integer anno;

    @Column(unique = true)
    private String targa;

    @Column(unique = true)
    private String vin;  // telaio

    @Column(nullable = false)
    private Double prezzoBase;

    private Integer chilometraggio;      // per usato

    private String alimentazione;        // benzina, diesel, elettrico, ...

    private String cambio;               // manuale, automatico

    private String coloreEsterno;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoVeicolo stato = StatoVeicolo.DISPONIBILE;

    @Column(nullable = false)
    private boolean visibileAlPubblico = true;

    private LocalDateTime dataInserimento;
    private LocalDateTime dataUltimoAggiornamento;
}