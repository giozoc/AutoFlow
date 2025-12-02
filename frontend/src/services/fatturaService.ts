// src/services/fatturaService.ts

import axios from 'axios'
import { authHeaders, getAuthState } from './authService'
import type { FatturaDTO } from '../entities/fattura'

const API_BASE_URL = 'http://localhost:8080/api/fatture'

// già usato in AdminPropostaEditPage
export async function generaFatturaDaProposta(
    propostaId: number,
): Promise<FatturaDTO> {
    const res = await axios.post<FatturaDTO>(
        `${API_BASE_URL}/da-proposta/${propostaId}`,
        null,
        { headers: authHeaders() },
    )
    return res.data
}

// elenco completo (admin/addetto)
export async function getAllFatture(): Promise<FatturaDTO[]> {
    const res = await axios.get<FatturaDTO[]>(API_BASE_URL, {
        headers: authHeaders(),
    })
    return res.data
}

// elenco fatture per cliente loggato
export async function getFattureCliente(): Promise<FatturaDTO[]> {
    const { userId } = getAuthState()
    if (!userId) {
        throw new Error('Cliente non loggato')
    }

    const res = await axios.get<FatturaDTO[]>(
        `${API_BASE_URL}/cliente/${userId}`,
        { headers: authHeaders() },
    )
    return res.data
}

// singola fattura (se in futuro vuoi una pagina di dettaglio)
export async function getFattura(id: number): Promise<FatturaDTO> {
    const res = await axios.get<FatturaDTO>(`${API_BASE_URL}/${id}`, {
        headers: authHeaders(),
    })
    return res.data
}

export async function downloadFatturaPdf(
    id: number,
    filename?: string,
): Promise<void> {
    const res = await axios.get(`${API_BASE_URL}/${id}/pdf`, {
        headers: authHeaders(),
        responseType: 'blob',
    })

    const blob = new Blob([res.data], { type: 'application/pdf' })
    const url = window.URL.createObjectURL(blob)

    const link = document.createElement('a')
    link.href = url
    link.download = filename ?? `fattura-${id}.pdf`

    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
}