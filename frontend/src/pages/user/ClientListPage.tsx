// src/pages/user/ClientListPage.tsx
import { useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import type { ClienteDTO } from '../../entities/client'
import {
    getAllClienti,
    createCliente,
    updateCliente,
    deleteCliente,
} from '../../services/clientService'
import { getAuthState, logout } from '../../services/authService'

import '../../styles/adminclienti.css'

function emptyCliente(): ClienteDTO {
    return {
        nome: '',
        cognome: '',
        email: '',
        telefono: '',
        indirizzo: '',
        attivo: true,
        codiceFiscale: '',
        dataNascita: '',
    }
}

const ClientListPage: React.FC = () => {
    const [clienti, setClienti] = useState<ClienteDTO[]>([])
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)
    const [form, setForm] = useState<ClienteDTO>(emptyCliente())
    const [saving, setSaving] = useState(false)
    const [editingId, setEditingId] = useState<number | null>(null)

    const auth = getAuthState()
    const navigate = useNavigate()
    const isAdmin = auth.ruolo === 'AMMINISTRATORE'

    async function loadClienti() {
        setLoading(true)
        setError(null)
        try {
            const data = await getAllClienti()
            setClienti(data)
        } catch (e) {
            console.error(e)
            // 🔴 messaggio invariato
            setError('Errore nel caricamento dei clienti.')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        void loadClienti()
    }, [])

    function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
        const { name, value, type, checked } = e.target
        setForm((prev) => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value,
        }))
    }

    function startEdit(cliente: ClienteDTO) {
        setEditingId(cliente.id ?? null)
        setForm({ ...cliente })
    }

    function cancelEdit() {
        setEditingId(null)
        setForm(emptyCliente())
    }

    async function handleSubmit(e: FormEvent) {
        e.preventDefault()
        setSaving(true)
        setError(null)
        try {
            if (editingId != null) {
                const updated = await updateCliente(editingId, form)
                setClienti((prev) =>
                    prev.map((c) => (c.id === editingId ? updated : c)),
                )
            } else {
                const created = await createCliente(form)
                setClienti((prev) => [...prev, created])
            }

            setForm(emptyCliente())
            setEditingId(null)
        } catch (err) {
            console.error(err)
            setError(
                editingId != null
                    ? "Errore nell'aggiornamento del cliente."
                    : 'Errore nella creazione del cliente.',
            )
        } finally {
            setSaving(false)
        }
    }

    async function handleDelete(id?: number) {
        if (!id) return
        if (!confirm('Eliminare questo cliente?')) return
        try {
            const ok = await deleteCliente(id)
            if (ok) {
                setClienti((prev) => prev.filter((c) => c.id !== id))
            }
        } catch (err) {
            console.error(err)
            alert("Errore durante l'eliminazione")
        }
    }

    async function handleLogout() {
        await logout()
        navigate('/login', { replace: true })
    }

    return (
        <div className="admincli-page">
            <div className="admincli-inner">
                {/* NAVBAR ADMIN */}
                <header className="admincli-header">
                    <div className="admincli-logo-block">
                        <div className="admincli-logo">
                            AutoFlow – <span>Area gestionale</span>
                        </div>
                        <p className="admincli-subtitle">
                            Loggato come <strong>{auth.ruolo}</strong> (userId:{' '}
                            {auth.userId})
                        </p>
                    </div>

                    <div className="admincli-header-right">

                        <nav className="admincli-nav">
                            <Link
                                to="/"
                                className="showroom-header-link"
                            >
                                Profilo
                            </Link>
                            {isAdmin && (
                                <Link
                                    to="/admin/staff"
                                    className="admincli-nav-link"
                                >
                                    Gestione addetti vendita
                                </Link>
                            )}

                            <Link
                                to="/admin/clienti"
                                className="admincli-nav-link admincli-nav-link--active"
                            >
                                Gestione clienti
                            </Link>
                            <Link
                                to="/admin/veicoli"
                                className="admincli-nav-link"
                            >
                                Gestione veicoli
                            </Link>
                            <Link
                                to="/admin/optional"
                                className="admincli-nav-link"
                            >
                                Catalogo optional
                            </Link>
                            <Link
                                to="/admin/configurazioni"
                                className="admincli-nav-link"
                            >
                                Configurazioni
                            </Link>
                            <Link
                                to="/admin/proposte"
                                className="admincli-nav-link"
                            >
                                Gestione proposte
                            </Link>
                            <Link
                                to="/admin/fatture"
                                className="admincli-nav-link"
                            >
                                Fatture
                            </Link>
                            <Link
                                to="/admin/statistics"
                                className="admincli-nav-link"
                            >
                                Statistiche
                            </Link>
                        </nav>

                        <button
                            className="admincli-logout-btn"
                            onClick={handleLogout}
                        >
                            Logout
                        </button>
                    </div>
                </header>

                {/* TITOLO PAGINA */}
                <h1 className="admincli-title">Gestione clienti</h1>

                {error && <p style={{ color: 'red' }}>{error}</p>}

                {/* LAYOUT FORM + TABELLA */}
                <main className="admincli-layout">
                    {/* FORM CLIENTE */}
                    <section className="admincli-card admincli-card--form">
                        <h2 className="admincli-card-title">
                            {editingId != null
                                ? 'Modifica cliente'
                                : 'Nuovo cliente'}
                        </h2>

                        <form
                            onSubmit={handleSubmit}
                            className="admincli-form-grid"
                        >
                            <div className="admincli-form-group">
                                <label className="admincli-label" htmlFor="nome">
                                    Nome
                                </label>
                                <input
                                    id="nome"
                                    name="nome"
                                    placeholder="Nome"
                                    value={form.nome}
                                    onChange={handleChange}
                                    required
                                    className="admincli-input"
                                />
                            </div>

                            <div className="admincli-form-group">
                                <label
                                    className="admincli-label"
                                    htmlFor="cognome"
                                >
                                    Cognome
                                </label>
                                <input
                                    id="cognome"
                                    name="cognome"
                                    placeholder="Cognome"
                                    value={form.cognome}
                                    onChange={handleChange}
                                    required
                                    className="admincli-input"
                                />
                            </div>

                            <div className="admincli-form-group">
                                <label
                                    className="admincli-label"
                                    htmlFor="email"
                                >
                                    Email
                                </label>
                                <input
                                    id="email"
                                    name="email"
                                    type="email"
                                    placeholder="Email"
                                    value={form.email}
                                    onChange={handleChange}
                                    required
                                    className="admincli-input"
                                />
                            </div>

                            <div className="admincli-form-group">
                                <label
                                    className="admincli-label"
                                    htmlFor="telefono"
                                >
                                    Telefono
                                </label>
                                <input
                                    id="telefono"
                                    name="telefono"
                                    placeholder="Telefono"
                                    value={form.telefono ?? ''}
                                    onChange={handleChange}
                                    className="admincli-input"
                                />
                            </div>

                            <div className="admincli-form-group admincli-form-group--full">
                                <label
                                    className="admincli-label"
                                    htmlFor="indirizzo"
                                >
                                    Indirizzo
                                </label>
                                <input
                                    id="indirizzo"
                                    name="indirizzo"
                                    placeholder="Indirizzo"
                                    value={form.indirizzo}
                                    onChange={handleChange}
                                    required
                                    className="admincli-input"
                                />
                            </div>

                            <div className="admincli-form-group admincli-form-group--full">
                                <label
                                    className="admincli-label"
                                    htmlFor="codiceFiscale"
                                >
                                    Codice Fiscale
                                </label>
                                <input
                                    id="codiceFiscale"
                                    name="codiceFiscale"
                                    placeholder="Codice Fiscale"
                                    value={form.codiceFiscale}
                                    onChange={handleChange}
                                    required
                                    className="admincli-input"
                                />
                            </div>

                            <div className="admincli-form-group admincli-form-group--full">
                                <label
                                    className="admincli-label"
                                    htmlFor="dataNascita"
                                >
                                    Data di Nascita
                                </label>
                                <input
                                    id="dataNascita"
                                    name="dataNascita"
                                    type="date"
                                    placeholder="Data di Nascita"
                                    value={form.dataNascita}
                                    onChange={handleChange}
                                    required
                                    className="admincli-input"
                                />
                            </div>

                            <label className="admincli-checkbox-row">
                                <input
                                    type="checkbox"
                                    name="attivo"
                                    checked={form.attivo}
                                    onChange={handleChange}
                                />{' '}
                                Cliente attivo
                            </label>

                            <div className="admincli-form-actions">
                                <button
                                    type="submit"
                                    disabled={saving}
                                    className="admincli-btn-primary"
                                >
                                    {saving
                                        ? editingId != null
                                            ? 'Salvataggio...'
                                            : 'Creazione...'
                                        : editingId != null
                                            ? 'Salva modifiche'
                                            : 'Crea cliente'}
                                </button>

                                {editingId != null && (
                                    <button
                                        type="button"
                                        onClick={cancelEdit}
                                        className="admincli-btn-secondary"
                                    >
                                        Annulla modifica
                                    </button>
                                )}
                            </div>
                        </form>
                    </section>

                    {/* TABELLA CLIENTI */}
                    <section className="admincli-card admincli-card--table">
                        <div className="admincli-table-header">
                            <h2 className="admincli-card-title">
                                Elenco clienti
                            </h2>
                            <p className="admincli-table-subtitle">
                                {loading
                                    ? 'Caricamento...'
                                    : clienti.length === 0
                                        ? 'Nessun cliente presente.'
                                        : `${clienti.length} clienti trovati`}
                            </p>
                        </div>

                        {loading ? (
                            <p>Caricamento...</p>
                        ) : clienti.length === 0 ? (
                            <p>Nessun cliente presente.</p>
                        ) : (
                            <div className="admincli-table-wrapper">
                                <table className="admincli-table">
                                    <thead>
                                    <tr>
                                        <th>Nome</th>
                                        <th>Cognome</th>
                                        <th>Email</th>
                                        <th>Telefono</th>
                                        <th>Indirizzo</th>
                                        <th>Attivo</th>
                                        <th>Azioni</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {clienti.map((c) => (
                                        <tr key={c.id}>
                                            <td>{c.nome}</td>
                                            <td>{c.cognome}</td>
                                            <td>{c.email}</td>
                                            <td>{c.telefono}</td>
                                            <td>{c.indirizzo}</td>
                                            <td>
                                                    <span
                                                        className={
                                                            c.attivo
                                                                ? 'admincli-badge admincli-badge--active'
                                                                : 'admincli-badge admincli-badge--inactive'
                                                        }
                                                    >
                                                        {c.attivo ? 'Sì' : 'No'}
                                                    </span>
                                            </td>
                                            <td>
                                                <div className="admincli-table-actions">
                                                    <button
                                                        type="button"
                                                        onClick={() =>
                                                            startEdit(c)
                                                        }
                                                        className="admincli-table-btn"
                                                    >
                                                        Modifica
                                                    </button>
                                                    <button
                                                        type="button"
                                                        onClick={() =>
                                                            handleDelete(
                                                                c.id,
                                                            )
                                                        }
                                                        className="admincli-table-btn admincli-table-btn--danger"
                                                    >
                                                        Elimina
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </section>
                </main>
            </div>
        </div>
    )
}

export default ClientListPage