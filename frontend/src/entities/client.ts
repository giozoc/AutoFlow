// src/entities/client.ts

export interface ClienteDTO {
    id?: number
    nome: string
    cognome: string
    email: string
    telefono?: string
    indirizzo: string
    attivo: boolean
    codiceFiscale: string
    dataNascita: string
    password?: string
}