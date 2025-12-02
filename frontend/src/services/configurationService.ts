// src/services/configurationService.ts
import axios from 'axios'
import type { ConfigurazioneDTO } from '../entities/configuration'
import { authHeaders } from './authService'

const API_BASE_URL = 'http://localhost:8080'

export async function getAllConfigurazioni(): Promise<ConfigurazioneDTO[]> {
    const res = await axios.get<ConfigurazioneDTO[]>(`${API_BASE_URL}/api/configurazioni`, {
        headers: authHeaders(),
    })
    return res.data
}

export async function getConfigurazioneById(id: number): Promise<ConfigurazioneDTO> {
    const res = await axios.get<ConfigurazioneDTO>(`${API_BASE_URL}/api/configurazioni/${id}`, {
        headers: authHeaders(),
    })
    return res.data
}

export async function createConfigurazione(
    dto: ConfigurazioneDTO,
): Promise<ConfigurazioneDTO> {
    const res = await axios.post<ConfigurazioneDTO>(`${API_BASE_URL}/api/configurazioni`, dto, {
        headers: authHeaders(),
    })
    return res.data
}

export async function updateConfigurazione(
    id: number,
    dto: ConfigurazioneDTO,
): Promise<ConfigurazioneDTO> {
    const res = await axios.put<ConfigurazioneDTO>(`${API_BASE_URL}/api/configurazioni/${id}`, dto, {
        headers: authHeaders(),
    })
    return res.data
}

export async function deleteConfigurazione(id: number): Promise<boolean> {
    const res = await axios.delete(`${API_BASE_URL}/api/configurazioni/${id}`, {
        headers: authHeaders(),
    })
    return res.status === 204 || res.status === 200
}