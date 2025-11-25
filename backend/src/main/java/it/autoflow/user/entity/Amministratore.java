package it.autoflow.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "amministratori")
public class Amministratore extends User {

    private String nome;
    private String cognome;
}