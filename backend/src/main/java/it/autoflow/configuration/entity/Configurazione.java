package it.autoflow.configuration.entity;

import it.autoflow.user.entity.Cliente;
import it.autoflow.vehicle.entity.Veicolo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter @Setter
@Entity
@Table(name = "configurazioni")
public class Configurazione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // cliente proprietario della configurazione
    @ManyToOne(optional = false)
    private Cliente cliente;

    // veicolo configurato
    @ManyToOne(optional = false)
    private Veicolo veicolo;

    // optional scelti
    @ManyToMany
    @JoinTable(
            name = "configurazioni_optional",
            joinColumns = @JoinColumn(name = "configurazione_id"),
            inverseJoinColumns = @JoinColumn(name = "optional_id")
    )
    private Set<OptionalAccessorio> optional = new HashSet<>();

    @Column(nullable = false)
    private Double prezzoBase;

    @Column(nullable = false)
    private Double prezzoTotale;

    private String note;

    private LocalDateTime dataCreazione;
    private LocalDateTime ultimaModifica;
}