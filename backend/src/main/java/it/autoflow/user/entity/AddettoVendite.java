package it.autoflow.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "addetti_vendite")
public class AddettoVendite extends User {

    private String nome;
    private String cognome;
    private String matricola;
}