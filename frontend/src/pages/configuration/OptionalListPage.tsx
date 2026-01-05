// src/pages/configuration/OptionalListPage.tsx
import { useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import type { OptionalAccessorioDTO } from '../../entities/optional'
import {
    getAllOptional,
    createOptional,
    updateOptional,
    deleteOptional,
} from '../../services/optionalService'
import { getAuthState, logout } from '../../services/authService'

import '../../styles/adminoptional.css'

function emptyOptional(): OptionalAccessorioDTO {
    return {
        codice: '',
        nome: '',
        descrizione: '',
        prezzo: 0,
    }
}

const OptionalListPage: React.FC = () => {
    const [optionals, setOptionals] = useState<OptionalAccessorioDTO[]>([])
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)
    const [form, setForm] = useState<OptionalAccessorioDTO>(emptyOptional())
    const [saving, setSaving] = useState(false)
    const [editingId, setEditingId] = useState<number | null>(null)

    const auth = getAuthState()
    const navigate = useNavigate()
    const isAdmin = auth.ruolo === 'AMMINISTRATORE'

    async function loadOptionals() {
        setLoading(true)
        setError(null)
        try {
            const data = await getAllOptional()
            setOptionals(data)
        } catch (e) {
            console.error(e)
            // messaggio invariato
            setError('Errore nel caricamento degli optional.')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        void loadOptionals()
    }, [])

    function handleChange(
        e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>,
    ) {
        const { name, value } = e.target
        if (name === 'prezzo') {
            setForm((prev) => ({
                ...prev,
                prezzo: value === '' ? 0 : Number(value),
            }))
        } else {
            setForm((prev) => ({ ...prev, [name]: value }))
        }
    }

    function startEdit(opt: OptionalAccessorioDTO) {
        setEditingId(opt.id ?? null)
        setForm({ ...opt })
    }

    function cancelEdit() {
        setEditingId(null)
        setForm(emptyOptional())
    }

    async function handleSubmit(e: FormEvent) {
        e.preventDefault()
        setSaving(true)
        setError(null)
        try {
            if (editingId != null) {
                const updated = await updateOptional(editingId, form)
                setOptionals((prev) =>
                    prev.map((o) => (o.id === editingId ? updated : o)),
                )
            } else {
                const created = await createOptional(form)
                setOptionals((prev) => [...prev, created])
            }

            setForm(emptyOptional())
            setEditingId(null)
        } catch (err) {
            console.error(err)
            setError(
                editingId != null
                    ? "Errore nell'aggiornamento dell'optional."
                    : "Errore nella creazione dell'optional.",
            )
        } finally {
            setSaving(false)
        }
    }

    async function handleDelete(id?: number) {
        if (!id) return
        if (!confirm('Eliminare questo optional/accessorio?')) return
        try {
            const ok = await deleteOptional(id)
            if (ok) {
                setOptionals((prev) => prev.filter((o) => o.id !== id))
            }
        } catch (err) {
            console.error(err)
            alert("Errore durante l'eliminazione dell'optional.")
        }
    }

    async function handleLogout() {
        await logout()
        navigate('/login', { replace: true })
    }

    return (
        <div className="adminopt-page">
            <div className="adminopt-inner">
                {/* NAVBAR ADMIN */}
                <header className="adminopt-header">
                    <div className="adminopt-logo-block">
                        <div className="adminopt-logo">
                            AutoFlow – <span>Area gestionale</span>
                        </div>
                        <p className="adminopt-subtitle">
                            Loggato come <strong>{auth.ruolo}</strong> (userId:{' '}
                            {auth.userId})
                        </p>
                    </div>

                    <div className="adminopt-header-right">
                        <nav className="adminopt-nav">
                            <Link
                                to="/"
                                className="showroom-header-link"
                            >
                                Profilo
                            </Link>

                            {isAdmin && (
                                <Link
                                    to="/admin/staff"
                                    className="adminopt-nav-link"
                                >
                                    Gestione addetti vendita
                                </Link>
                            )}

                            <Link
                                to="/admin/clienti"
                                className="adminopt-nav-link"
                            >
                                Gestione clienti
                            </Link>
                            <Link
                                to="/admin/veicoli"
                                className="adminopt-nav-link"
                            >
                                Gestione veicoli
                            </Link>
                            <Link
                                to="/admin/optional"
                                className="adminopt-nav-link adminopt-nav-link--active"
                            >
                                Catalogo optional
                            </Link>
                            <Link
                                to="/admin/configurazioni"
                                className="adminopt-nav-link"
                            >
                                Configurazioni
                            </Link>
                            <Link
                                to="/admin/proposte"
                                className="adminopt-nav-link"
                            >
                                Gestione proposte
                            </Link>
                            <Link
                                to="/admin/fatture"
                                className="adminopt-nav-link"
                            >
                                Fatture
                            </Link>
                            <Link
                                to="/admin/statistics"
                                className="adminopt-nav-link"
                            >
                                Statistiche
                            </Link>
                        </nav>

                        <button
                            className="adminopt-logout-btn"
                            onClick={handleLogout}
                        >
                            Logout
                        </button>
                    </div>
                </header>

                {/* TITOLO */}
                <h1 className="adminopt-title">
                    Catalogo optional / accessori
                </h1>

                {error && <p style={{ color: 'red' }}>{error}</p>}

                {/* LAYOUT FORM + TABELLA */}
                <main className="adminopt-layout">
                    {/* FORM OPTIONAL */}
                    <section className="adminopt-card adminopt-card--form">
                        <h2 className="adminopt-card-title">
                            {editingId != null
                                ? 'Modifica optional'
                                : 'Nuovo optional'}
                        </h2>

                        <form
                            onSubmit={handleSubmit}
                            className="adminopt-form-grid"
                        >
                            <div className="adminopt-form-group">
                                <label
                                    htmlFor="codice"
                                    className="adminopt-label"
                                >
                                    Codice
                                </label>
                                <input
                                    id="codice"
                                    name="codice"
                                    placeholder="Codice (es. PKG-TECH-01)"
                                    value={form.codice}
                                    onChange={handleChange}
                                    required
                                    className="adminopt-input"
                                />
                            </div>

                            <div className="adminopt-form-group">
                                <label
                                    htmlFor="nome"
                                    className="adminopt-label"
                                >
                                    Nome
                                </label>
                                <input
                                    id="nome"
                                    name="nome"
                                    placeholder="Nome (es. Pacchetto Tech)"
                                    value={form.nome}
                                    onChange={handleChange}
                                    required
                                    className="adminopt-input"
                                />
                            </div>

                            <div className="adminopt-form-group adminopt-form-group--full">
                                <label
                                    htmlFor="descrizione"
                                    className="adminopt-label"
                                >
                                    Descrizione
                                </label>
                                <textarea
                                    id="descrizione"
                                    name="descrizione"
                                    placeholder="Descrizione"
                                    value={form.descrizione}
                                    onChange={handleChange}
                                    className="adminopt-textarea"
                                />
                            </div>

                            <div className="adminopt-form-group">
                                <label
                                    htmlFor="prezzo"
                                    className="adminopt-label"
                                >
                                    Prezzo
                                </label>
                                <input
                                    id="prezzo"
                                    name="prezzo"
                                    type="number"
                                    step="0.01"
                                    placeholder="Prezzo"
                                    value={form.prezzo ?? 0}
                                    onChange={handleChange}
                                    required
                                    className="adminopt-input"
                                />
                            </div>

                            <div className="adminopt-form-actions">
                                <button
                                    type="submit"
                                    disabled={saving}
                                    className="adminopt-btn-primary"
                                >
                                    {saving
                                        ? editingId != null
                                            ? 'Salvataggio...'
                                            : 'Creazione...'
                                        : editingId != null
                                            ? 'Salva modifiche'
                                            : 'Crea optional'}
                                </button>

                                {editingId != null && (
                                    <button
                                        type="button"
                                        onClick={cancelEdit}
                                        className="adminopt-btn-secondary"
                                    >
                                        Annulla modifica
                                    </button>
                                )}
                            </div>
                        </form>
                    </section>

                    {/* LISTA OPTIONAL */}
                    <section className="adminopt-card adminopt-card--table">
                        <div className="adminopt-table-header">
                            <h2 className="adminopt-card-title">
                                Elenco optional
                            </h2>
                            <p className="adminopt-table-subtitle">
                                {loading
                                    ? 'Caricamento...'
                                    : optionals.length === 0
                                        ? 'Nessun optional presente.'
                                        : `${optionals.length} optional trovati`}
                            </p>
                        </div>

                        {loading ? (
                            <p>Caricamento...</p>
                        ) : optionals.length === 0 ? (
                            <p>Nessun optional presente.</p>
                        ) : (
                            <div className="adminopt-table-wrapper">
                                <table className="adminopt-table">
                                    <thead>
                                    <tr>
                                        <th>Codice</th>
                                        <th>Nome</th>
                                        <th>Descrizione</th>
                                        <th>Prezzo</th>
                                        <th>Azioni</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {optionals.map((o) => (
                                        <tr key={o.id}>
                                            <td>{o.codice}</td>
                                            <td>{o.nome}</td>
                                            <td>{o.descrizione}</td>
                                            <td>
                                                {o.prezzo.toLocaleString(
                                                    'it-IT',
                                                )}{' '}
                                                €
                                            </td>
                                            <td>
                                                <div className="adminopt-table-actions">
                                                    <button
                                                        type="button"
                                                        onClick={() =>
                                                            startEdit(o)
                                                        }
                                                        className="adminopt-table-btn"
                                                    >
                                                        Modifica
                                                    </button>
                                                    <button
                                                        type="button"
                                                        onClick={() =>
                                                            handleDelete(
                                                                o.id,
                                                            )
                                                        }
                                                        className="adminopt-table-btn adminopt-table-btn--danger"
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

export default OptionalListPage