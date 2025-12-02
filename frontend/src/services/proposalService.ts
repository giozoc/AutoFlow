// src/services/proposalService.ts

import axios from 'axios';
import { authHeaders, getAuthState } from './authService';
import type {
    CreatePropostaDTO,
    PropostaDTO,
    StatoProposta,
} from '../entities/proposal';

const API_BASE_URL = 'http://localhost:8080/api/proposte';

// ---------- LETTURA ----------

// tutte le proposte (admin/addetto)
export async function getTutteLeProposte(): Promise<PropostaDTO[]> {
    const res = await axios.get<PropostaDTO[]>(API_BASE_URL, {
        headers: authHeaders(),
    });
    return res.data;
}

// proposte per cliente loggato
export async function getProposteCliente(): Promise<PropostaDTO[]> {
    const { userId } = getAuthState();
    if (!userId) throw new Error('Cliente non loggato');

    const res = await axios.get<PropostaDTO[]>(
        `${API_BASE_URL}/cliente/${userId}`,
        { headers: authHeaders() }
    );
    return res.data;
}

// proposte per addetto vendite loggato (se vuoi usarlo in futuro)
export async function getProposteAddetto(): Promise<PropostaDTO[]> {
    const { userId } = getAuthState();
    if (!userId) throw new Error('Addetto non loggato');

    const res = await axios.get<PropostaDTO[]>(
        `${API_BASE_URL}/addetto/${userId}`,
        { headers: authHeaders() }
    );
    return res.data;
}

// singola proposta
export async function getProposta(id: number): Promise<PropostaDTO> {
    const res = await axios.get<PropostaDTO>(`${API_BASE_URL}/${id}`, {
        headers: authHeaders(),
    });
    return res.data;
}

// ---------- SCRITTURA ----------

// creazione da configurazione lato cliente
export async function createProposta(
    dto: CreatePropostaDTO
): Promise<PropostaDTO> {
    // Il backend si aspetta un PropostaDTO completo (clienteId, addettoVenditeId,
    // configurazioneId, prezzoProposta, stato non-null).
    const body: Omit<PropostaDTO, 'id' | 'dataCreazione'> & { dataCreazione?: string | null } = {
        clienteId: dto.clienteId,
        addettoVenditeId: dto.addettoVenditeId!,
        configurazioneId: dto.configurazioneId,
        prezzoProposta: dto.prezzoProposta,
        stato: 'INVIATA',      // importante: lo stato non può essere null
        dataCreazione: null,   // il backend, se null, mette LocalDate.now()
        dataScadenza: null,
        noteCliente: dto.noteCliente ?? null,
        noteInterne: null,
    };

    const res = await axios.post<PropostaDTO>(API_BASE_URL, body, {
        headers: authHeaders(),
    });
    return res.data;
}

// update completo (admin/addetto)
export async function updateProposta(
    id: number,
    dto: PropostaDTO
): Promise<PropostaDTO> {
    const res = await axios.put<PropostaDTO>(`${API_BASE_URL}/${id}`, dto, {
        headers: authHeaders(),
    });
    return res.data;
}

// cambio stato (riusa updateProposta)
export async function changeStatoProposta(
    id: number,
    nuovoStato: StatoProposta
): Promise<PropostaDTO> {
    const corrente = await getProposta(id);

    const updated: PropostaDTO = {
        ...corrente,
        stato: nuovoStato,
    };

    return updateProposta(id, updated);
}

// per il cliente
export async function acceptProposta(id: number): Promise<PropostaDTO> {
    // conferma finale da parte del cliente
    return changeStatoProposta(id, 'COMPLETATA')
}

export async function rejectProposta(id: number): Promise<PropostaDTO> {
    return changeStatoProposta(id, 'RIFIUTATA');
}