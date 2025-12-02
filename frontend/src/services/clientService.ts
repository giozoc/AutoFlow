// src/services/clientService.ts
import axios from 'axios'
import type { ClienteDTO } from '../entities/client'
import { authHeaders } from './authService'

const API_BASE_URL = 'http://localhost:8080'

export async function getClienteById(id: number): Promise<ClienteDTO> {
    const res = await axios.get<ClienteDTO>(`${API_BASE_URL}/clienti/${id}`, {
        headers: authHeaders(),
    })
    return res.data
}

export async function getAllClienti(): Promise<ClienteDTO[]> {
    const res = await axios.get<ClienteDTO[]>(`${API_BASE_URL}/clienti`, {
        headers: authHeaders(),
    })
    return res.data
}

export async function createCliente(cliente: ClienteDTO): Promise<ClienteDTO> {
    const res = await axios.post<ClienteDTO>(`${API_BASE_URL}/clienti`, cliente, {
        headers: authHeaders(),
    })
    return res.data
}

export async function updateCliente(
    id: number,
    cliente: ClienteDTO,
): Promise<ClienteDTO> {
    const res = await axios.put<ClienteDTO>(`${API_BASE_URL}/clienti/${id}`, cliente, {
        headers: authHeaders(),
    })
    return res.data
}

export async function deleteCliente(id: number): Promise<boolean> {
    const res = await axios.delete<boolean>(`${API_BASE_URL}/clienti/${id}`, {
        headers: authHeaders(),
    })
    return res.data
}