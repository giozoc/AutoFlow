// src/entities/proposal.ts

export type StatoProposta =
    | 'BOZZA'
    | 'INVIATA'
    | 'ACCETTATA'
    | 'RIFIUTATA'
    | 'SCADUTA'
    | 'ANNULLATA'
    | 'COMPLETATA';

export interface PropostaDTO {
    id: number;
    clienteId: number;
    addettoVenditeId: number;
    configurazioneId: number;
    prezzoProposta: number;
    stato: StatoProposta;
    dataCreazione: string;      // LocalDate -> string ISO es "2025-11-30"
    dataScadenza: string | null;

    noteCliente?: string | null;
    noteInterne?: string | null;
}

/**
 * DTO che usiamo lato FE per creare una proposta da una configurazione.
 * Lo convertiamo in PropostaDTO quando chiamiamo il backend.
 */
export interface CreatePropostaDTO {
    clienteId: number;
    addettoVenditeId?: number| null;
    configurazioneId: number;
    prezzoProposta: number;
    noteCliente?: string | null;
}