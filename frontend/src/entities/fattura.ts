// src/entities/fattura.ts

export interface FatturaDTO {
    id: number;
    propostaId: number;
    clienteId: number;
    dataFattura: string;      // es. "2025-11-30"
    importoTotale: number;
    numeroFattura: string;

    note?: string | null;
}