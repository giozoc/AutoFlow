// src/entities/statistics.ts

// Deve rispecchiare DashboardStatisticsDTO del backend
export interface DashboardStatisticsDTO {
    // --- Entità base ---
    totaleClienti: number;
    totaleVeicoli: number;
    totaleProposte: number;
    totaleFatture: number;

    // --- Proposte per stato ---
    proposteBozza: number;
    proposteInviate: number;
    proposteAccettate: number;
    proposteRifiutate: number;
    proposteScadute: number;
    proposteAnnullate: number;
    proposteCompletate: number;

    // --- Fatture / fatturato ---
    fatturatoTotale: number;
    fatturatoAnnoCorrente: number;
    fatturatoMeseCorrente: number;
    fattureNonPagate: number;
}