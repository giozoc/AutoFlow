import axios from 'axios';
import type { LoginRequestDTO, LoginResponseDTO, Ruolo } from '../entities/auth';
import type { RegisterClienteDTO } from '../entities/auth';

const API_BASE_URL = 'http://localhost:8080';
const STORAGE_KEY = 'autoflow_auth';

export interface AuthState {
    token: string | null;
    ruolo: Ruolo | null;
    userId: number | null;
}

const EMPTY_AUTH: AuthState = {
    token: null,
    ruolo: null,
    userId: null,
};

export function getAuthState(): AuthState {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return { ...EMPTY_AUTH };

    try {
        const parsed = JSON.parse(raw);
        return {
            token: parsed.token ?? null,
            ruolo: parsed.ruolo ?? null,
            userId: parsed.userId ?? null,
        };
    } catch {
        return { ...EMPTY_AUTH };
    }
}

export function setAuthState(state: AuthState) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
}

export function clearAuthState() {
    localStorage.removeItem(STORAGE_KEY);
}

export async function login(
    request: LoginRequestDTO,
): Promise<AuthState & { mustChangePassword?: boolean }> {
    const response = await axios.post<LoginResponseDTO>(
        `${API_BASE_URL}/auth/login`,
        request,
    );

    const { token, ruolo, userId, mustChangePassword } = response.data;

    const authState: AuthState = {
        token,
        ruolo,
        userId,
    };

    setAuthState(authState);
    return { ...authState, mustChangePassword };
}


export async function logout(): Promise<void> {
    const { token } = getAuthState();
    if (token) {
        try {
            await axios.post(
                `${API_BASE_URL}/auth/logout`,
                {},
                {
                    headers: {
                        Authorization: `Bearer ${token}`,   // FIX IMPORTANTE
                    },
                }
            );
        } catch {
            // ignora errori backend
        }
    }
    clearAuthState();
}

export function authHeaders() {
    const { token } = getAuthState();
    return token
        ? {
            Authorization: `Bearer ${token}`,        // FIX IMPORTANTE
        }
        : {};
}

export async function registerCliente(dto: RegisterClienteDTO): Promise<void> {
    await axios.post(`${API_BASE_URL}/auth/register/cliente`, dto);
}