// src/entities/user.ts
export type RuoloUtente = 'CLIENTE' | 'ADDETTO_VENDITE' | 'AMMINISTRATORE'

export interface AddettoVenditeDTO {
    id?: number
    nome: string
    cognome: string
    matricola: string
    email: string
    telefono: string
    password?: string   // obbligatoria solo in creazione
    attivo: boolean
    ruolo: RuoloUtente
}