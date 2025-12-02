// src/services/vehicleService.ts
import axios from 'axios'
import type { VeicoloDTO, StatoVeicolo } from '../entities/vehicle'
import { authHeaders } from './authService'

const API_BASE_URL = 'http://localhost:8080'

export async function getVeicoliShowroom(): Promise<VeicoloDTO[]> {
    // endpoint backend: GET /veicoli/showroom
    const res = await axios.get<VeicoloDTO[]>(`${API_BASE_URL}/veicoli/showroom`)
    return res.data
}

export async function getAllVeicoli(): Promise<VeicoloDTO[]> {
    const res = await axios.get<VeicoloDTO[]>(`${API_BASE_URL}/veicoli`, {
        headers: authHeaders(),
    })
    return res.data
}

export async function createVeicolo(veicolo: VeicoloDTO): Promise<VeicoloDTO> {
    const res = await axios.post<VeicoloDTO>(`${API_BASE_URL}/veicoli`, veicolo, {
        headers: authHeaders(),
    })
    return res.data
}


export async function updateVeicolo(id: number, veicolo: VeicoloDTO): Promise<VeicoloDTO> {
    const res = await axios.put<VeicoloDTO>(`${API_BASE_URL}/veicoli/${id}`, veicolo, {
        headers: authHeaders(),
    })
    return res.data
}


export async function deleteVeicolo(id: number): Promise<boolean> {
    const res = await axios.delete(`${API_BASE_URL}/veicoli/${id}`, {
        headers: authHeaders(),
    })
    return res.status === 204 || res.status === 200
}


// Backend: POST /veicoli/{id}/duplicate
export async function duplicateVeicolo(id: number): Promise<VeicoloDTO> {
    const res = await axios.post<VeicoloDTO>(
        `${API_BASE_URL}/veicoli/${id}/duplicate`,
        {},
        { headers: authHeaders() },
    )
    return res.data
}


// Backend: POST /veicoli/{id}/stato?stato=DISPONIBILE|OPZIONATO|VENDUTO|NON_VISIBILE
export async function cambiaStatoVeicolo(
    id: number,
    nuovoStato: StatoVeicolo,
): Promise<VeicoloDTO> {
    const res = await axios.post<VeicoloDTO>(
        `${API_BASE_URL}/veicoli/${id}/stato`,
        {},
        {
            headers: authHeaders(),
            params: { stato: nuovoStato },
        },
    )
    return res.data
}

// Dettaglio singolo veicolo
export async function getVeicoloById(id: number): Promise<VeicoloDTO> {
    const res = await axios.get<VeicoloDTO>(`${API_BASE_URL}/veicoli/${id}`, {
        headers: authHeaders(),
    })
    return res.data
}