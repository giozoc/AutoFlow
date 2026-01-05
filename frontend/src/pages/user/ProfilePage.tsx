import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'

import { getAuthState } from '../../services/authService'
import {
    getProfiloCorrente,
    updateProfilo,
    type ProfiloDTO,
} from '../../services/profileService'
import type { ClienteDTO } from '../../entities/client'
import type { AddettoVenditeDTO } from '../../entities/user'
import { updateCliente } from '../../services/clientService'
import { updateStaff } from '../../services/staffService'

import '../../styles/profile.css'

const ProfilePage: React.FC = () => {
    const navigate = useNavigate()
    const auth = getAuthState()

    const [profilo, setProfilo] = useState<ProfiloDTO | null>(null)
    const [loading, setLoading] = useState(true)
    const [saving, setSaving] = useState(false)
    const [error, setError] = useState<string | null>(null)
    const [success, setSuccess] = useState<string | null>(null)

    // stato per CAMBIO PASSWORD
    const [oldPassword, setOldPassword] = useState('')
    const [newPassword, setNewPassword] = useState('')
    const [confirmPassword, setConfirmPassword] = useState('')
    const [pwdLoading, setPwdLoading] = useState(false)
    const [pwdError, setPwdError] = useState<string | null>(null)
    const [pwdSuccess, setPwdSuccess] = useState<string | null>(null)

    const isCliente = auth.ruolo === 'CLIENTE'
    const isAddetto = auth.ruolo === 'ADDETTO_VENDITE'
    const isAllowed = isCliente || isAddetto

    // 🔐 limita l’accesso: solo CLIENTE e ADDETTO_VENDITE
    useEffect(() => {
        if (!isAllowed) {
            navigate('/admin', { replace: true })
        }
    }, [isAllowed, navigate])

    // caricamento profilo corrente
    useEffect(() => {
        if (!isAllowed) return

            ;(async () => {
            try {
                setLoading(true)
                setError(null)
                const p = await getProfiloCorrente()
                setProfilo(p)
            } catch (e) {
                console.error(e)
                setError('Errore nel caricamento del profilo.')
            } finally {
                setLoading(false)
            }
        })()
    }, [isAllowed])

    function handleBack() {
        if (auth.ruolo === 'CLIENTE') {
            navigate('/')
        } else {
            navigate('/admin')
        }
    }

    function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
        if (!profilo) return
        const { name, value } = e.target

        setProfilo((prev) =>
            prev ? ({ ...prev, [name]: value } as ProfiloDTO) : prev,
        )
    }

    async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault()
        if (!profilo) return

        setSaving(true)
        setError(null)
        setSuccess(null)

        try {
            const updated = await updateProfilo(profilo)
            setProfilo(updated)
            setSuccess('Profilo aggiornato correttamente.')
        } catch (err) {
            console.error(err)

            //Mostra il messaggio esatto del backend se presente
            if (err instanceof Error && err.message) {
                setError(err.message) // es: "Nome troppo corto", "Numero di telefono non valido", ...
            } else {
                setError('Errore durante il salvataggio del profilo.')
            }
        } finally {
            setSaving(false)
        }
    }

    // 🔁 CAMBIO PASSWORD usando updateCliente / updateStaff
    async function handleChangePassword(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault()
        setPwdError(null)
        setPwdSuccess(null)

        if (!profilo) {
            setPwdError('Profilo non disponibile.')
            return
        }

        if (!oldPassword || !newPassword || !confirmPassword) {
            setPwdError('Compila tutti i campi.')
            return
        }

        if (newPassword !== confirmPassword) {
            setPwdError('Le nuove password non coincidono.')
            return
        }

        // NB: oldPassword NON viene inviato al backend, esattamente come nei form admin.
        // Se in futuro vuoi la validazione lato server, bisogna aggiungere un endpoint dedicato.

        setPwdLoading(true)
        try {
            if (!auth.userId) {
                setPwdError('Utente non autenticato.')
                return
            }

            if (auth.ruolo === 'CLIENTE') {
                const clientePayload: ClienteDTO = {
                    ...(profilo as ClienteDTO),
                    // assicurati che ClienteDTO abbia password?: string
                    password: newPassword as any,
                }
                const updated = await updateCliente(auth.userId, clientePayload)
                setProfilo(updated)
            } else if (auth.ruolo === 'ADDETTO_VENDITE') {
                const addettoPayload: AddettoVenditeDTO = {
                    ...(profilo as AddettoVenditeDTO),
                    password: newPassword,
                }
                const updated = await updateStaff(auth.userId, addettoPayload)
                setProfilo(updated)
            } else {
                setPwdError(
                    'Ruolo non abilitato al cambio password da questa pagina.',
                )
                return
            }

            setPwdSuccess('Password aggiornata correttamente.')
            setOldPassword('')
            setNewPassword('')
            setConfirmPassword('')
        } catch (err) {
            console.error(err)
            setPwdError('Errore durante il cambio password.')
        } finally {
            setPwdLoading(false)
        }
    }

    if (!isAllowed) {
        // reindirizzo gestito nell’useEffect
        return null
    }

    if (loading) {
        return <p>Caricamento profilo…</p>
    }

    if (!profilo) {
        return <p>Profilo non trovato.</p>
    }

    const ruoloLabel = auth.ruolo

    const profCliente = profilo as ClienteDTO
    const profStaff = profilo as AddettoVenditeDTO

    return (
        <div className="profile-page">
            <div className="profile-inner">
                {/* HEADER */}
                <header className="profile-header">
                    <div>
                        <h1 className="profile-title">Il mio profilo</h1>
                        <p className="profile-subtitle">
                            Visualizza e aggiorna i tuoi dati personali.
                        </p>
                        <p className="profile-role">
                            Ruolo: <strong>{ruoloLabel}</strong> (userId:{' '}
                            {auth.userId})
                        </p>
                    </div>

                    <div className="profile-header-actions">
                        <button
                            type="button"
                            className="profile-back-btn"
                            onClick={handleBack}
                        >
                            ← Torna indietro
                        </button>
                    </div>
                </header>

                {/* MESSAGGI PROFILO */}
                {error && (
                    <p
                        className="profile-message profile-message--error"
                        data-test="profile-error"
                    >
                        {error}
                    </p>
                )}
                {success && (
                    <p
                        className="profile-message profile-message--success"
                        data-test="toast-success"
                    >
                        {success}
                    </p>
                )}

                {/* CARD PROFILO */}
                <main className="profile-layout">
                    <section className="profile-card">
                        <h2 className="profile-card-title">Dati personali</h2>

                        <form
                            className="profile-form-grid"
                            onSubmit={handleSubmit}
                        >
                            {/* NOME */}
                            <div className="profile-form-group">
                                <label
                                    className="profile-label"
                                    htmlFor="nome"
                                >
                                    Nome
                                </label>
                                <input
                                    id="nome"
                                    name="nome"
                                    className="profile-input"
                                    value={profilo.nome}
                                    onChange={handleChange}
                                    required
                                />
                            </div>

                            {/* COGNOME */}
                            <div className="profile-form-group">
                                <label
                                    className="profile-label"
                                    htmlFor="cognome"
                                >
                                    Cognome
                                </label>
                                <input
                                    id="cognome"
                                    name="cognome"
                                    className="profile-input"
                                    value={profilo.cognome}
                                    onChange={handleChange}
                                    required
                                />
                            </div>

                            {/* EMAIL */}
                            <div className="profile-form-group profile-form-group--full">
                                <label
                                    className="profile-label"
                                    htmlFor="email"
                                >
                                    Email
                                </label>
                                <input
                                    id="email"
                                    name="email"
                                    type="email"
                                    className="profile-input"
                                    value={profilo.email}
                                    onChange={handleChange}
                                    required
                                />
                            </div>

                            {/* TELEFONO */}
                            <div className="profile-form-group profile-form-group--full">
                                <label
                                    className="profile-label"
                                    htmlFor="telefono"
                                >
                                    Telefono
                                </label>
                                <input
                                    id="telefono"
                                    name="telefono"
                                    className="profile-input"
                                    value={profilo.telefono ?? ''}
                                    onChange={handleChange}
                                />
                            </div>

                            {isCliente ? (
                                <>
                                    {/* CLIENTE: INDIRIZZO */}
                                    <div className="profile-form-group profile-form-group--full">
                                        <label
                                            className="profile-label"
                                            htmlFor="indirizzo"
                                        >
                                            Indirizzo
                                        </label>
                                        <input
                                            id="indirizzo"
                                            name="indirizzo"
                                            className="profile-input"
                                            value={profCliente.indirizzo ?? ''}
                                            onChange={handleChange}
                                        />
                                    </div>

                                    {/* CLIENTE: CF */}
                                    <div className="profile-form-group profile-form-group--full">
                                        <label
                                            className="profile-label"
                                            htmlFor="codiceFiscale"
                                        >
                                            Codice fiscale
                                        </label>
                                        <input
                                            id="codiceFiscale"
                                            name="codiceFiscale"
                                            className="profile-input"
                                            value={
                                                profCliente.codiceFiscale ?? ''
                                            }
                                            onChange={handleChange}
                                        />
                                    </div>

                                    {/* CLIENTE: DATA NASCITA */}
                                    <div className="profile-form-group profile-form-group--full">
                                        <label
                                            className="profile-label"
                                            htmlFor="dataNascita"
                                        >
                                            Data di nascita
                                        </label>
                                        <input
                                            id="dataNascita"
                                            name="dataNascita"
                                            type="date"
                                            className="profile-input"
                                            value={
                                                profCliente.dataNascita ?? ''
                                            }
                                            onChange={handleChange}
                                        />
                                    </div>
                                </>
                            ) : (
                                <>
                                    {/* STAFF: MATRICOLA (solo lettura) */}
                                    <div className="profile-form-group profile-form-group--full">
                                        <label
                                            className="profile-label"
                                            htmlFor="matricola"
                                        >
                                            Matricola
                                        </label>
                                        <input
                                            id="matricola"
                                            name="matricola"
                                            className="profile-input"
                                            value={profStaff.matricola ?? ''}
                                            disabled
                                        />
                                    </div>

                                    {/* STAFF: RUOLO (solo lettura) */}
                                    <div className="profile-form-group profile-form-group--full">
                                        <label className="profile-label">
                                            Ruolo
                                        </label>
                                        <input
                                            className="profile-input"
                                            value={profStaff.ruolo}
                                            disabled
                                        />
                                    </div>
                                </>
                            )}

                            {/* BOTTONI */}
                            <div className="profile-form-actions">
                                <button
                                    type="submit"
                                    className="profile-btn-primary"
                                    disabled={saving}
                                    data-test="salva-profilo"
                                >
                                    {saving
                                        ? 'Salvataggio…'
                                        : 'Salva modifiche'}
                                </button>
                            </div>
                        </form>
                    </section>

                    {/* SEZIONE CAMBIO PASSWORD */}
                    <section className="profile-card">
                        <h2 className="profile-card-title">
                            Cambia password
                        </h2>

                        {pwdError && (
                            <p className="profile-message profile-message--error">
                                {pwdError}
                            </p>
                        )}
                        {pwdSuccess && (
                            <p className="profile-message profile-message--success">
                                {pwdSuccess}
                            </p>
                        )}

                        <form
                            className="profile-form-grid profile-form-grid--single"
                            onSubmit={handleChangePassword}
                        >
                            <div className="profile-form-group profile-form-group--full">
                                <label
                                    className="profile-label"
                                    htmlFor="oldPassword"
                                >
                                    Password attuale
                                </label>
                                <input
                                    id="oldPassword"
                                    name="oldPassword"
                                    type="password"
                                    className="profile-input"
                                    value={oldPassword}
                                    onChange={(e) =>
                                        setOldPassword(e.target.value)
                                    }
                                />
                            </div>

                            <div className="profile-form-group profile-form-group--full">
                                <label
                                    className="profile-label"
                                    htmlFor="newPassword"
                                >
                                    Nuova password
                                </label>
                                <input
                                    id="newPassword"
                                    name="newPassword"
                                    type="password"
                                    className="profile-input"
                                    value={newPassword}
                                    onChange={(e) =>
                                        setNewPassword(e.target.value)
                                    }
                                />
                            </div>

                            <div className="profile-form-group profile-form-group--full">
                                <label
                                    className="profile-label"
                                    htmlFor="confirmPassword"
                                >
                                    Conferma nuova password
                                </label>
                                <input
                                    id="confirmPassword"
                                    name="confirmPassword"
                                    type="password"
                                    className="profile-input"
                                    value={confirmPassword}
                                    onChange={(e) =>
                                        setConfirmPassword(e.target.value)
                                    }
                                />
                            </div>

                            <div className="profile-form-actions">
                                <button
                                    type="submit"
                                    className="profile-btn-primary"
                                    disabled={pwdLoading}
                                >
                                    {pwdLoading
                                        ? 'Aggiornamento…'
                                        : 'Cambia password'}
                                </button>
                            </div>
                        </form>
                    </section>
                </main>
            </div>
        </div>
    )
}

export default ProfilePage
