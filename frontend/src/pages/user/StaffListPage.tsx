// src/pages/user/StaffListPage.tsx
import { useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import type { AddettoVenditeDTO } from '../../entities/user'
import {
    getAllStaff,
    createStaff,
    deleteStaff,
    toggleActive,
    resetStaffPassword,
    updateStaff,
} from '../../services/staffService'
import { getAuthState, logout } from '../../services/authService'

import './adminstaff.css'

function emptyAddetto(): AddettoVenditeDTO {
    return {
        id: undefined,
        nome: '',
        cognome: '',
        matricola: '',
        email: '',
        telefono: '',
        password: '',
        attivo: true,
        ruolo: 'ADDETTO_VENDITE',
    }
}

const StaffListPage: React.FC = () => {
    const [staff, setStaff] = useState<AddettoVenditeDTO[]>([])
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)
    const [form, setForm] = useState<AddettoVenditeDTO>(emptyAddetto())
    const [saving, setSaving] = useState(false)
    const [editingId, setEditingId] = useState<number | null>(null)

    const auth = getAuthState()
    const navigate = useNavigate()
    const isAdmin = auth.ruolo === 'AMMINISTRATORE'

    async function loadStaff() {
        setLoading(true)
        setError(null)
        try {
            const data = await getAllStaff()
            setStaff(data)
        } catch (e) {
            console.error(e)
            setError('Errore nel caricamento degli addetti.')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        void loadStaff()
    }, [])

    function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
        const { name, value, type, checked } = e.target
        setForm((prev) => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value,
        }))
    }

    async function handleSubmit(e: FormEvent) {
        e.preventDefault()
        setSaving(true)
        setError(null)
        try {
            if (editingId != null) {
                // MODIFICA ESISTENTE
                const updated = await updateStaff(editingId, {
                    ...form,
                    id: editingId,
                    // se lasci password vuota, non vogliamo toccarla
                    password: form.password || undefined,
                })
                setStaff((prev) =>
                    prev.map((a) => (a.id === editingId ? updated : a)),
                )
            } else {
                // CREAZIONE NUOVO
                const created = await createStaff(form)
                setStaff((prev) => [...prev, created])
            }

            // reset form
            setForm(emptyAddetto())
            setEditingId(null)
        } catch (err) {
            console.error(err)
            setError(
                editingId != null
                    ? "Errore nell'aggiornamento dell'addetto."
                    : "Errore nella creazione dell'addetto.",
            )
        } finally {
            setSaving(false)
        }
    }

    function startEdit(addetto: AddettoVenditeDTO) {
        setEditingId(addetto.id ?? null)
        setForm({
            ...addetto,
            // non mostriamo la password attuale; se l'admin ne mette una nuova, la usiamo
            password: '',
        })
    }

    function cancelEdit() {
        setEditingId(null)
        setForm(emptyAddetto())
    }

    async function handleDelete(id?: number) {
        if (!id) return
        if (!confirm('Sei sicuro di voler eliminare questo addetto?')) return
        try {
            const ok = await deleteStaff(id)
            if (ok) {
                setStaff((prev) => prev.filter((a) => a.id !== id))
            }
        } catch (err) {
            console.error(err)
            alert("Errore durante l'eliminazione")
        }
    }

    async function handleToggle(id?: number) {
        if (!id) return
        try {
            const updated = await toggleActive(id)
            if (updated) {
                setStaff((prev) =>
                    prev.map((a) => (a.id === id ? updated : a)),
                )
            }
        } catch (err) {
            console.error(err)
            alert('Errore durante aggiornamento stato')
        }
    }

    async function handleResetPassword(id?: number) {
        if (!id) return
        if (!confirm('Reset password per questo addetto?')) return
        try {
            const ok = await resetStaffPassword(id)
            if (ok) {
                alert(
                    'Password resettata (controlla la logica nel backend per il nuovo valore).',
                )
            }
        } catch (err) {
            console.error(err)
            alert('Errore durante reset password')
        }
    }

    async function handleLogout() {
        await logout()
        navigate('/login', { replace: true })
    }

    return (
        <div className="adminstaff-page">
            <div className="adminstaff-inner">
                {/* NAVBAR ADMIN */}
                <header className="adminstaff-header">
                    <div className="adminstaff-logo-block">
                        <div className="adminstaff-logo">
                            AutoFlow – <span>Area gestionale</span>
                        </div>
                        <p className="adminstaff-subtitle">
                            Loggato come <strong>{auth.ruolo}</strong> (userId:{' '}
                            {auth.userId})
                        </p>
                    </div>

                    <div className="adminstaff-header-right">
                        <nav className="adminstaff-nav">
                            <Link
                                to="/"
                                className="showroom-header-link"
                            >
                                Profilo
                            </Link>
                            {isAdmin && (
                                <Link
                                    to="/admin/staff"
                                    className="adminstaff-nav-link adminstaff-nav-link--active"
                                >
                                    Gestione addetti vendita
                                </Link>
                            )}

                            <Link
                                to="/admin/clienti"
                                className="adminstaff-nav-link"
                            >
                                Gestione clienti
                            </Link>
                            <Link
                                to="/admin/veicoli"
                                className="adminstaff-nav-link"
                            >
                                Gestione veicoli
                            </Link>
                            <Link
                                to="/admin/optional"
                                className="adminstaff-nav-link"
                            >
                                Catalogo optional
                            </Link>
                            <Link
                                to="/admin/configurazioni"
                                className="adminstaff-nav-link"
                            >
                                Configurazioni
                            </Link>
                            <Link
                                to="/admin/proposte"
                                className="adminstaff-nav-link"
                            >
                                Gestione proposte
                            </Link>
                            <Link
                                to="/admin/fatture"
                                className="adminstaff-nav-link"
                            >
                                Fatture
                            </Link>
                            <Link
                                to="/admin/statistics"
                                className="adminstaff-nav-link"
                            >
                                Statistiche
                            </Link>
                        </nav>

                        <button
                            className="adminstaff-logout-btn"
                            onClick={handleLogout}
                        >
                            Logout
                        </button>
                    </div>
                </header>

                {/* TITOLO */}
                <h1 className="adminstaff-title">
                    Gestione addetti alle vendite
                </h1>

                {error && <p style={{ color: 'red' }}>{error}</p>}

                {/* LAYOUT FORM + TABELLA */}
                <main className="adminstaff-layout">
                    {/* FORM ADDETTO */}
                    <section className="adminstaff-card adminstaff-card--form">
                        <h2 className="adminstaff-card-title">
                            {editingId != null
                                ? 'Modifica addetto'
                                : 'Nuovo addetto'}
                        </h2>

                        <form
                            onSubmit={handleSubmit}
                            className="adminstaff-form-grid"
                        >
                            <div className="adminstaff-form-group">
                                <label
                                    className="adminstaff-label"
                                    htmlFor="nome"
                                >
                                    Nome
                                </label>
                                <input
                                    id="nome"
                                    name="nome"
                                    placeholder="Nome"
                                    value={form.nome}
                                    onChange={handleChange}
                                    required
                                    className="adminstaff-input"
                                />
                            </div>

                            <div className="adminstaff-form-group">
                                <label
                                    className="adminstaff-label"
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
                                    className="adminstaff-input"
                                />
                            </div>

                            <div className="adminstaff-form-group">
                                <label
                                    className="adminstaff-label"
                                    htmlFor="matricola"
                                >
                                    Matricola
                                </label>
                                <input
                                    id="matricola"
                                    name="matricola"
                                    placeholder="Matricola"
                                    value={form.matricola}
                                    onChange={handleChange}
                                    required
                                    className="adminstaff-input"
                                />
                            </div>

                            <div className="adminstaff-form-group">
                                <label
                                    className="adminstaff-label"
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
                                    className="adminstaff-input"
                                />
                            </div>

                            <div className="adminstaff-form-group">
                                <label
                                    className="adminstaff-label"
                                    htmlFor="telefono"
                                >
                                    Telefono
                                </label>
                                <input
                                    id="telefono"
                                    name="telefono"
                                    placeholder="Telefono"
                                    value={form.telefono}
                                    onChange={handleChange}
                                    required
                                    className="adminstaff-input"
                                />
                            </div>

                            <div className="adminstaff-form-group">
                                <label
                                    className="adminstaff-label"
                                    htmlFor="password"
                                >
                                    Password
                                </label>
                                <input
                                    id="password"
                                    name="password"
                                    type="password"
                                    placeholder={
                                        editingId != null
                                            ? 'Nuova password (opzionale)'
                                            : 'Password iniziale'
                                    }
                                    value={form.password ?? ''}
                                    onChange={handleChange}
                                    // in modifica non obblighiamo la password
                                    required={editingId == null}
                                    className="adminstaff-input"
                                />
                            </div>

                            <label className="adminstaff-checkbox-row">
                                <input
                                    type="checkbox"
                                    name="attivo"
                                    checked={form.attivo}
                                    onChange={handleChange}
                                />{' '}
                                Addetto attivo
                            </label>

                            <div className="adminstaff-form-actions">
                                <button
                                    type="submit"
                                    disabled={saving}
                                    className="adminstaff-btn-primary"
                                >
                                    {saving
                                        ? editingId != null
                                            ? 'Salvataggio...'
                                            : 'Creazione...'
                                        : editingId != null
                                            ? 'Salva modifiche'
                                            : 'Crea addetto'}
                                </button>

                                {editingId != null && (
                                    <button
                                        type="button"
                                        onClick={cancelEdit}
                                        className="adminstaff-btn-secondary"
                                    >
                                        Annulla modifica
                                    </button>
                                )}
                            </div>
                        </form>
                    </section>

                    {/* TABELLA ADDETTI */}
                    <section className="adminstaff-card adminstaff-card--table">
                        <div className="adminstaff-table-header">
                            <h2 className="adminstaff-card-title">
                                Elenco addetti
                            </h2>
                            <p className="adminstaff-table-subtitle">
                                {loading
                                    ? 'Caricamento...'
                                    : staff.length === 0
                                        ? 'Nessun addetto presente.'
                                        : `${staff.length} addetti trovati`}
                            </p>
                        </div>

                        {loading ? (
                            <p>Caricamento...</p>
                        ) : staff.length === 0 ? (
                            <p>Nessun addetto presente.</p>
                        ) : (
                            <div className="adminstaff-table-wrapper">
                                <table className="adminstaff-table">
                                    <thead>
                                    <tr>
                                        <th>Nome</th>
                                        <th>Cognome</th>
                                        <th>Matricola</th>
                                        <th>Email</th>
                                        <th>Telefono</th>
                                        <th>Attivo</th>
                                        <th>Azioni</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {staff.map((a) => (
                                        <tr key={a.id}>
                                            <td>{a.nome}</td>
                                            <td>{a.cognome}</td>
                                            <td>{a.matricola}</td>
                                            <td>{a.email}</td>
                                            <td>{a.telefono}</td>
                                            <td>
                                                    <span
                                                        className={
                                                            a.attivo
                                                                ? 'adminstaff-badge adminstaff-badge--active'
                                                                : 'adminstaff-badge adminstaff-badge--inactive'
                                                        }
                                                    >
                                                        {a.attivo ? 'Sì' : 'No'}
                                                    </span>
                                            </td>
                                            <td>
                                                <div className="adminstaff-table-actions">
                                                    <button
                                                        type="button"
                                                        onClick={() =>
                                                            startEdit(a)
                                                        }
                                                        className="adminstaff-table-btn"
                                                    >
                                                        Modifica
                                                    </button>
                                                    <button
                                                        type="button"
                                                        onClick={() =>
                                                            handleToggle(
                                                                a.id,
                                                            )
                                                        }
                                                        className="adminstaff-table-btn"
                                                    >
                                                        Toggle attivo
                                                    </button>
                                                    <button
                                                        type="button"
                                                        onClick={() =>
                                                            handleResetPassword(
                                                                a.id,
                                                            )
                                                        }
                                                        className="adminstaff-table-btn"
                                                    >
                                                        Reset pw
                                                    </button>
                                                    <button
                                                        type="button"
                                                        onClick={() =>
                                                            handleDelete(
                                                                a.id,
                                                            )
                                                        }
                                                        className="adminstaff-table-btn adminstaff-table-btn--danger"
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

export default StaffListPage