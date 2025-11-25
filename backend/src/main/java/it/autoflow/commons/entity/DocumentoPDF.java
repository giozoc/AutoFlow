package it.autoflow.commons.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@Entity
@Table(name = "documenti_pdf")
public class DocumentoPDF {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // nome file "fattura-1234.pdf"
    @Column(nullable = false)
    private String nomeFile;

    // path sul file system, es: "/var/autoflow/docs/fatture/fattura-1234.pdf"
    @Column(nullable = false)
    private String path;

    private Long dimensioneBytes;

    private LocalDateTime dataCreazione;
    private LocalDateTime ultimaModifica;
}