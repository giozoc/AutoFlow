// src/entities/vehicle.ts

export type StatoVeicolo = 'DISPONIBILE' | 'OPZIONATO' | 'VENDUTO' | 'NON_VISIBILE'

export interface VeicoloDTO {
    id?: number
    marca: string
    modello: string
    anno: number
    targa: string
    vin: string
    prezzoBase: number
    chilometraggio: number
    alimentazione: string
    cambio: string
    coloreEsterno: string
    stato: StatoVeicolo
    visibileAlPubblico: boolean
}