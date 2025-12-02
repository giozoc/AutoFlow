export type Ruolo = 'CLIENTE' | 'ADDETTO_VENDITE' | 'AMMINISTRATORE';

export interface LoginRequestDTO {
    email: string;
    password: string;
}

export interface LoginResponseDTO {
    token: string;
    ruolo: Ruolo;
    userId: number;
    mustChangePassword?: boolean;   // nuovo
}


export interface RegisterClienteDTO {
    nome: string
    cognome: string
    email: string
    password: string
    telefono: string
    indirizzo: string
    codiceFiscale: string
    dataNascita: string
}