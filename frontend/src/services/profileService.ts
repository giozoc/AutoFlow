// src/services/profileService.ts

import axios from 'axios'
import type { ClienteDTO } from '../entities/client'
import type { AddettoVenditeDTO } from '../entities/user'
import { getAuthState } from './authService'
import { getClienteById, updateCliente } from './clientService'
import { getAllStaff, updateStaff } from './staffService'

// Il profilo può essere un cliente o uno staff (addetto/admin)
export type ProfiloDTO = ClienteDTO | AddettoVenditeDTO

// Carica i dati del profilo corrente in base al ruolo
export async function getProfiloCorrente(): Promise<ProfiloDTO> {
    const auth = getAuthState()

    if (!auth.userId || !auth.ruolo) {
        throw new Error('Utente non autenticato')
    }

    // --- CLIENTE ---
    if (auth.ruolo === 'CLIENTE') {
        const cliente = await getClienteById(auth.userId)
        return cliente
    }

    // --- ADDETTO_VENDITE / AMMINISTRATORE ---
    const staff = await getAllStaff()
    const addetto = staff.find((a) => a.id === auth.userId)

    if (!addetto) {
        throw new Error('Utente staff non trovato')
    }

    return addetto
}

// Salva modifiche profilo
export async function updateProfilo(profilo: ProfiloDTO): Promise<ProfiloDTO> {
    const auth = getAuthState()

    if (!auth.userId || !auth.ruolo) {
        throw new Error('Utente non autenticato')
    }

    try {
        if (auth.ruolo === 'CLIENTE') {
            const updated = await updateCliente(auth.userId, profilo as ClienteDTO)
            return updated
        } else {
            // ADDETTO_VENDITE o AMMINISTRATORE
            const payload: AddettoVenditeDTO = {
                ...(profilo as AddettoVenditeDTO),
                // dalla pagina profilo NON tocchiamo la password
                password: '',
            }

            const updated = await updateStaff(auth.userId, payload)
            return updated
        }
    } catch (err: any) {
        // 🔎 Mappiamo i messaggi di errore del backend (TC2_xx) in una stringa da mostrare nel FE
        let message = 'Errore durante il salvataggio del profilo.'

        if (axios.isAxiosError(err)) {
            const data = err.response?.data

            if (typeof data === 'string') {
                // es: backend restituisce direttamente "Nome troppo corto"
                message = data
            } else if (data && typeof (data as any).message === 'string') {
                // es: { "message": "Nome troppo corto" }
                message = (data as any).message
            }
        } else if (err instanceof Error && err.message) {
            message = err.message
        }

        // possibili messaggi:
        //  - "Nome troppo corto"
        //  - "Nome troppo lungo"
        //  - "Formato nome non valido"
        //  - "Cognome troppo corto"
        //  - "Cognome troppo lungo"
        //  - "Formato cognome non valido"
        //  - "Numero di telefono non valido"
        //  - "Formato telefono non valido"
        //  - "Account disattivato"
        throw new Error(message)
    }
}
