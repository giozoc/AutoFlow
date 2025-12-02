// src/services/showroomService.ts
import axios from 'axios'
import type { ShowroomFiltro } from '../entities/showroom'
import type { VeicoloDTO } from '../entities/vehicle'

const API_BASE_URL = 'http://localhost:8080'

// Ricerca veicoli per lo showroom pubblico
export async function searchShowroom(
    filtro: ShowroomFiltro,
): Promise<VeicoloDTO[]> {
    const res = await axios.post<VeicoloDTO[]>(
        `${API_BASE_URL}/showroom/search`,
        filtro,
    )
    return res.data
}

// Dettaglio pubblico di un singolo veicolo
export async function getShowroomDettaglio(
    id: number,
): Promise<VeicoloDTO> {
    const res = await axios.get<VeicoloDTO>(`${API_BASE_URL}/showroom/${id}`)
    return res.data
}