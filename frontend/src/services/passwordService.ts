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
    return res.data;
}

export async function changePasswordAfterReset(
    nuovaPassword: string,
): Promise<boolean> {
    const { token } = getAuthState();
    if (!token) {
        throw new Error('Token di sessione mancante');
    }

    const payload: PasswordFirstChangeDTO = {
        token,
        nuovaPassword,
    };

    const res = await axios.post<boolean>(
        `${API_BASE_URL}/auth/password/first-change`,
        payload,
    );
    return res.data;
}
