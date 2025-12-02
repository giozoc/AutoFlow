// src/entities/configuration.ts

export interface ConfigurazioneDTO {
    id?: number
    clienteId: number
    veicoloId: number
    prezzoBase: number
    prezzoTotale: number
    dataCreazione?: string // ISO string
    note?: string
    optionalIds: number[]
}