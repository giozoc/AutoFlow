package it.autoflow.statistics.dto;

import lombok.Data;

@Data
public class DashboardStatisticsDTO {

    // --- Entità base ---
    private Long totaleClienti;
    private Long totaleVeicoli;
    private Long totaleProposte;
    private Long totaleFatture;

    // --- Proposte per stato ---
    private Long proposteBozza;
    private Long proposteInviate;
    private Long proposteAccettate;
    private Long proposteRifiutate;
    private Long proposteScadute;
    private Long proposteAnnullate;
    private Long proposteCompletate;

    // --- Fatture / fatturato ---
    private Double fatturatoTotale;
    private Double fatturatoAnnoCorrente;
    private Double fatturatoMeseCorrente;
    private Long fattureNonPagate;
}