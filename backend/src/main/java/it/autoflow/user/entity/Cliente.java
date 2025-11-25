package it.autoflow.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
@Entity
@Table(name = "clienti")
public class Cliente extends User {

    private String nome;
    private String cognome;

    private String email;
    private String telefono;

    private String indirizzo;        // indirizzo di casa
    private String codiceFiscale;    // codice fiscale

    private LocalDate dataNascita;   // data di nascita
}