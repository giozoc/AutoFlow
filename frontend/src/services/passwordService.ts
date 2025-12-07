// src/services/passwordService.ts
import axios from 'axios';
import type {
    PasswordResetRequestDTO,
    PasswordResetResultDTO,
    PasswordFirstChangeDTO,
} from '../entities/password';
import { getAuthState } from './authService';

const API_BASE_URL = 'http://localhost:8080';

export async function forgotPassword(
    payload: PasswordResetRequestDTO,
): Promise<PasswordResetResultDTO> {
    const res = await axios.post<PasswordResetResultDTO>(
        `${API_BASE_URL}/auth/password/reset-request`,
        payload,
    );
    // Il backend restituisce sempre 200 con:
    //  - { success: true, defaultPassword: "Cliente123!" } se cliente esistente
    //  - { success: false, defaultPassword: null } se email nulla / vuota / inesistente
    return res.data;
}

export async function changePasswordAfterReset(
    nuovaPassword: string,
): Promise<boolean> {
    const { token } = getAuthState();
    if (!token) {
        throw new Error('Token di sessione mancante');
    }

    // IMPORTANTE per TC3: il backend si aspetta "Bearer <token>"
    const payload: PasswordFirstChangeDTO = {
        token: `Bearer ${token}`,
        nuovaPassword,
    };

    const res = await axios.post<boolean>(
        `${API_BASE_URL}/auth/password/first-change`,
        payload,
    );
    // Il backend restituisce:
    //  - true  se token valido e utente NON attivo (post reset) -> password aggiornata, attivo=true
    //  - false se token inesistente o utente già attivo
    return res.data;
}
