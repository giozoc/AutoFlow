// src/services/staffService.ts
import axios from 'axios'
import type { AddettoVenditeDTO } from '../entities/user'
import { authHeaders } from './authService'

const API_BASE_URL = 'http://localhost:8080'

export async function getAllStaff(): Promise<AddettoVenditeDTO[]> {
    const res = await axios.get<AddettoVenditeDTO[]>(`${API_BASE_URL}/staff`, {
        headers: authHeaders(),
    })
    return res.data
}

export async function createStaff(addetto: AddettoVenditeDTO): Promise<AddettoVenditeDTO> {
    const res = await axios.post<AddettoVenditeDTO>(`${API_BASE_URL}/staff`, addetto, {
        headers: authHeaders(),
    })
    return res.data
}

export async function updateStaff(
    id: number,
    addetto: AddettoVenditeDTO,
): Promise<AddettoVenditeDTO> {
    const res = await axios.put<AddettoVenditeDTO>(`${API_BASE_URL}/staff/${id}`, addetto, {
        headers: authHeaders(),
    })
    return res.data
}

export async function deleteStaff(id: number): Promise<boolean> {
    const res = await axios.delete<boolean>(`${API_BASE_URL}/staff/${id}`, {
        headers: authHeaders(),
    })
    return res.data
}

export async function toggleActive(id: number): Promise<AddettoVenditeDTO | null> {
    const res = await axios.post<AddettoVenditeDTO | null>(
        `${API_BASE_URL}/staff/${id}/toggle-active`,
        {},
        { headers: authHeaders() },
    )
    return res.data
}

export async function resetStaffPassword(id: number): Promise<boolean> {
    const res = await axios.post<boolean>(
        `${API_BASE_URL}/staff/${id}/reset-password`,
        {},
        { headers: authHeaders() },
    )
    return res.data
}