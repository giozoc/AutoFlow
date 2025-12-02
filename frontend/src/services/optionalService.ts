// src/services/optionalService.ts
import axios from 'axios'
import type { OptionalAccessorioDTO } from '../entities/optional'
import { authHeaders } from './authService'

const API_BASE_URL = 'http://localhost:8080'

export async function getAllOptional(): Promise<OptionalAccessorioDTO[]> {
    const res = await axios.get<OptionalAccessorioDTO[]>(
        `${API_BASE_URL}/api/optional`,
        {
            headers: authHeaders(),
        },
    )
    return res.data
}

export async function createOptional(
    dto: OptionalAccessorioDTO,
): Promise<OptionalAccessorioDTO> {
    const res = await axios.post<OptionalAccessorioDTO>(
        `${API_BASE_URL}/api/optional`,
        dto,
        {
            headers: authHeaders(),
        },
    )
    return res.data
}

export async function updateOptional(
    id: number,
    dto: OptionalAccessorioDTO,
): Promise<OptionalAccessorioDTO> {
    const res = await axios.put<OptionalAccessorioDTO>(
        `${API_BASE_URL}/api/optional/${id}`,
        dto,
        {
            headers: authHeaders(),
        },
    )
    return res.data
}

export async function deleteOptional(id: number): Promise<boolean> {
    const res = await axios.delete(`${API_BASE_URL}/api/optional/${id}`, {
        headers: authHeaders(),
    })
    return res.status === 204 || res.status === 200
}