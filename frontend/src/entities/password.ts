export interface PasswordResetRequestDTO {
    email: string;
}

export interface PasswordResetResultDTO {
    success: boolean;
    defaultPassword?: string | null;
}

export interface PasswordFirstChangeDTO {
    token: string;
    nuovaPassword: string;
}
