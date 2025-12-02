// src/pages/vehicle/VehicleListPage.tsx
import { useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import type { VeicoloDTO, StatoVeicolo } from '../../entities/vehicle'
import {
    getAllVeicoli,
    createVeicolo,
    updateVeicolo,
    deleteVeicolo,
    duplicateVeicolo,
    cambiaStatoVeicolo,
} from '../../services/vehicleService'
import { getAuthState, logout } from '../../services/authService'

import './adminveicoli.css'

function emptyVeicolo(): VeicoloDTO {
    return {
        marca: '',
        modello: '',
        anno: new Date().getFullYear(),
        targa: '',
        vin: '',
        prezzoBase: 0,
        chilometraggio: 0,
        alimentazione: '',
        cambio: '',
        coloreEsterno: '',
        stato: 'DISPONIBILE',
        visibileAlPubblico: true,
    }
}

const stati: StatoVeicolo[] = [
    'DISPONIBILE',
    'OPZIONATO',
    'VENDUTO',
    'NON_VISIBILE',
]

const VehicleListPage: React.FC = () => {
    const [veicoli, setVeicoli] = useState<VeicoloDTO[]>([])
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)
    const [form, setForm] = useState<VeicoloDTO>(emptyVeicolo())
    const [saving, setSaving] = useState(false)
    const [editingId, setEditingId] = useState<number | null>(null)

    const auth = getAuthState()
    const navigate = useNavigate()
    const isAdmin = auth.ruolo === 'AMMINISTRATORE'

    async function loadVeicoli() {
        setLoading(true)
        setError(null)
        try {
            const data = await getAllVeicoli()
            setVeicoli(data)
        } catch (e) {
            console.error(e)
            // 🔴 messaggio invariato
            setError('Errore nel caricamento dei veicoli.')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        void loadVeicoli()
    }, [])

    function handleChange(
        e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>,
    ) {
        const target = e.target
        const name = target.name
        let value: unknown = target.value

        if (target instanceof HTMLInputElement && target.type === 'checkbox') {
            value = target.checked
        }

        if (['anno', 'prezzoBase', 'chilometraggio'].includes(name)) {
            value = target.value === '' ? 0 : Number(target.value)
        }

        setForm((prev) => ({
            ...prev,
            [name]: value,
        }))
    }

    function startEdit(v: VeicoloDTO) {
        setEditingId(v.id ?? null)
        setForm({ ...v })
    }

    function cancelEdit() {
        setEditingId(null)
        setForm(emptyVeicolo())
    }

    async function handleSubmit(e: FormEvent) {
        e.preventDefault()
        setSaving(true)
        setError(null)
        try {
            if (editingId != null) {
                const updated = await updateVeicolo(editingId, form)
                setVeicoli((prev) =>
                    prev.map((v) => (v.id === editingId ? updated : v)),
                )
            } else {
                const created = await createVeicolo(form)
                setVeicoli((prev) => [...prev, created])
            }

            setForm(emptyVeicolo())
            setEditingId(null)
        } catch (err) {
            console.error(err)
            setError(
                editingId != null
                    ? "Errore nell'aggiornamento del veicolo."
                    : 'Errore nella creazione del veicolo.',
            )
        } finally {
            setSaving(false)
        }
    }

    async function handleDelete(id?: number) {
        if (!id) return
        if (!confirm('Eliminare questo veicolo?')) return
        try {
            const ok = await deleteVeicolo(id)
            if (ok) {
                setVeicoli((prev) => prev.filter((v) => v.id !== id))
            }
        } catch (err) {
            console.error(err)
            alert("Errore durante l'eliminazione")
        }
    }

    // 🔁 DUPLICA VEICOLO
    async function handleDuplicate(id?: number) {
        if (!id) return
        try {
            const duplicated = await duplicateVeicolo(id)
            setVeicoli((prev) => [...prev, duplicated])
        } catch (err) {
            console.error(err)
            alert('Errore durante la duplicazione del veicolo')
        }
    }

    // ⚡ CAMBIO STATO VELOCE
    async function handleChangeStato(
        id: number | undefined,
        stato: StatoVeicolo,
    ) {
        if (!id) return
        try {
            const updated = await cambiaStatoVeicolo(id, stato)
            setVeicoli((prev) =>
                prev.map((v) => (v.id === id ? updated : v)),
            )
        } catch (err) {
            console.error(err)
            alert('Errore durante il cambio di stato')
        }
    }

    async function handleLogout() {
        await logout()
        navigate('/login', { replace: true })
    }

    return (
        <div className="adminveh-page">
            <div className="adminveh-inner">
                {/* NAVBAR ADMIN */}
                <header className="adminveh-header">
                    <div className="adminveh-logo-block">
                        <div className="adminveh-logo">
                            AutoFlow – <span>Area gestionale</span>
                        </div>
                        <p className="adminveh-subtitle">
                            Loggato come <strong>{auth.ruolo}</strong> (userId:{' '}
                            {auth.userId})
                        </p>
                    </div>

                    <div className="adminveh-header-right">
                        <nav className="adminveh-nav">
                            <Link
                                to="/"
                                className="showroom-header-link"
                            >
                                Profilo
                            </Link>
                            {isAdmin && (
                                <Link
                                    to="/admin/staff"
                                    className="adminveh-nav-link"
                                >
                                    Gestione addetti vendita
                                </Link>
                            )}

                            <Link
                                to="/admin/clienti"
                                className="adminveh-nav-link"
                            >
                                Gestione clienti
                            </Link>
                            <Link
                                to="/admin/veicoli"
                                className="adminveh-nav-link adminveh-nav-link--active"
                            >
                                Gestione veicoli
                            </Link>
                            <Link
                                to="/admin/optional"
                                className="adminveh-nav-link"
                            >
                                Catalogo optional
                            </Link>
                            <Link
                                to="/admin/configurazioni"
                                className="adminveh-nav-link"
                            >
                                Configurazioni
                            </Link>
                            <Link
                                to="/admin/proposte"
                                className="adminveh-nav-link"
                            >
                                Gestione proposte
                            </Link>
                            <Link
                                to="/admin/fatture"
                                className="adminveh-nav-link"
                            >
                                Fatture
                            </Link>
                            <Link
                                to="/admin/statistics"
                                className="adminveh-nav-link"
                            >
                                Statistiche
                            </Link>
                        </nav>

                        <button
                            className="adminveh-logout-btn"
                            onClick={handleLogout}
                        >
                            Logout
                        </button>
                    </div>
                </header>

                {/* TITOLO */}
                <h1 className="adminveh-title">Gestione veicoli</h1>

                {error && <p style={{ color: 'red' }}>{error}</p>}

                {/* LAYOUT FORM + TABELLA */}
                <main className="adminveh-layout">
                    {/* FORM VEICOLO */}
                    <section className="adminveh-card adminveh-card--form">
                        <h2 className="adminveh-card-title">
                            {editingId != null
                                ? 'Modifica veicolo'
                                : 'Nuovo veicolo'}
                        </h2>

                        <form
                            onSubmit={handleSubmit}
                            className="adminveh-form-grid"
                        >
                            <div className="adminveh-form-group">
                                <label
                                    htmlFor="marca"
                                    className="adminveh-label"
                                >
                                    Marca
                                </label>
                                <input
                                    id="marca"
                                    name="marca"
                                    placeholder="Marca"
                                    value={form.marca}
                                    onChange={handleChange}
                                    required
                                    className="adminveh-input"
                                />
                            </div>

                            <div className="adminveh-form-group">
                                <label
                                    htmlFor="modello"
                                    className="adminveh-label"
                                >
                                    Modello
                                </label>
                                <input
                                    id="modello"
                                    name="modello"
                                    placeholder="Modello"
                                    value={form.modello}
                                    onChange={handleChange}
                                    required
                                    className="adminveh-input"
                                />
                            </div>

                            <div className="adminveh-form-group">
                                <label
                                    htmlFor="anno"
                                    className="adminveh-label"
                                >
                                    Anno
                                </label>
                                <input
                                    id="anno"
                                    name="anno"
                                    type="number"
                                    placeholder="Anno"
                                    value={form.anno ?? ''}
                                    onChange={handleChange}
                                    required
                                    className="adminveh-input"
                                />
                            </div>

                            <div className="adminveh-form-group">
                                <label
                                    htmlFor="targa"
                                    className="adminveh-label"
                                >
                                    Targa
                                </label>
                                <input
                                    id="targa"
                                    name="targa"
                                    placeholder="Targa"
                                    value={form.targa}
                                    onChange={handleChange}
                                    required
                                    className="adminveh-input"
                                />
                            </div>

                            <div className="adminveh-form-group">
                                <label
                                    htmlFor="vin"
                                    className="adminveh-label"
                                >
                                    VIN
                                </label>
                                <input
                                    id="vin"
                                    name="vin"
                                    placeholder="VIN"
                                    value={form.vin}
                                    onChange={handleChange}
                                    required
                                    className="adminveh-input"
                                />
                            </div>

                            <div className="adminveh-form-group">
                                <label
                                    htmlFor="prezzoBase"
                                    className="adminveh-label"
                                >
                                    Prezzo base
                                </label>
                                <input
                                    id="prezzoBase"
                                    name="prezzoBase"
                                    type="number"
                                    step="0.01"
                                    placeholder="Prezzo base"
                                    value={form.prezzoBase ?? ''}
                                    onChange={handleChange}
                                    required
                                    className="adminveh-input"
                                />
                            </div>

                            <div className="adminveh-form-group">
                                <label
                                    htmlFor="chilometraggio"
                                    className="adminveh-label"
                                >
                                    Chilometraggio
                                </label>
                                <input
                                    id="chilometraggio"
                                    name="chilometraggio"
                                    type="number"
                                    placeholder="Chilometraggio"
                                    value={form.chilometraggio ?? ''}
                                    onChange={handleChange}
                                    required
                                    className="adminveh-input"
                                />
                            </div>

                            <div className="adminveh-form-group">
                                <label
                                    htmlFor="alimentazione"
                                    className="adminveh-label"
                                >
                                    Alimentazione
                                </label>
                                <input
                                    id="alimentazione"
                                    name="alimentazione"
                                    placeholder="Benzina, diesel, ibrida..."
                                    value={form.alimentazione}
                                    onChange={handleChange}
                                    required
                                    className="adminveh-input"
                                />
                            </div>

                            <div className="adminveh-form-group">
                                <label
                                    htmlFor="cambio"
                                    className="adminveh-label"
                                >
                                    Cambio
                                </label>
                                <input
                                    id="cambio"
                                    name="cambio"
                                    placeholder="Manuale, automatico..."
                                    value={form.cambio}
                                    onChange={handleChange}
                                    required
                                    className="adminveh-input"
                                />
                            </div>

                            <div className="adminveh-form-group adminveh-form-group--full">
                                <label
                                    htmlFor="coloreEsterno"
                                    className="adminveh-label"
                                >
                                    Colore esterno
                                </label>
                                <input
                                    id="coloreEsterno"
                                    name="coloreEsterno"
                                    placeholder="Colore esterno"
                                    value={form.coloreEsterno}
                                    onChange={handleChange}
                                    required
                                    className="adminveh-input"
                                />
                            </div>

                            <div className="adminveh-form-group">
                                <label
                                    htmlFor="stato"
                                    className="adminveh-label"
                                >
                                    Stato
                                </label>
                                <select
                                    id="stato"
                                    name="stato"
                                    value={form.stato}
                                    onChange={handleChange}
                                    className="adminveh-input adminveh-select"
                                >
                                    {stati.map((s) => (
                                        <option key={s} value={s}>
                                            {s}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            <label className="adminveh-checkbox-row">
                                <input
                                    type="checkbox"
                                    name="visibileAlPubblico"
                                    checked={form.visibileAlPubblico}
                                    onChange={handleChange}
                                />{' '}
                                Visibile sul sito / showroom
                            </label>

                            <div className="adminveh-form-actions">
                                <button
                                    type="submit"
                                    disabled={saving}
                                    className="adminveh-btn-primary"
                                >
                                    {saving
                                        ? editingId != null
                                            ? 'Salvataggio...'
                                            : 'Creazione...'
                                        : editingId != null
                                            ? 'Salva modifiche'
                                            : 'Crea veicolo'}
                                </button>

                                {editingId != null && (
                                    <button
                                        type="button"
                                        onClick={cancelEdit}
                                        className="adminveh-btn-secondary"
                                    >
                                        Annulla modifica
                                    </button>
                                )}
                            </div>
                        </form>
                    </section>

                    {/* LISTA VEICOLI */}
                    <section className="adminveh-card adminveh-card--table">
                        <div className="adminveh-table-header">
                            <h2 className="adminveh-card-title">
                                Elenco veicoli
                            </h2>
                            <p className="adminveh-table-subtitle">
                                {loading
                                    ? 'Caricamento...'
                                    : veicoli.length === 0
                                        ? 'Nessun veicolo presente.'
                                        : `${veicoli.length} veicoli trovati`}
                            </p>
                        </div>

                        {loading ? (
                            <p>Caricamento...</p>
                        ) : veicoli.length === 0 ? (
                            <p>Nessun veicolo presente.</p>
                        ) : (
                            <div className="adminveh-table-wrapper">
                                <table className="adminveh-table">
                                    <thead>
                                    <tr>
                                        <th>Marca</th>
                                        <th>Modello</th>
                                        <th>Anno</th>
                                        <th>Targa</th>
                                        <th>VIN</th>
                                        <th>Stato</th>
                                        <th>Visibile</th>
                                        <th>Azioni</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {veicoli.map((v) => (
                                        <tr key={v.id}>
                                            <td>{v.marca}</td>
                                            <td>{v.modello}</td>
                                            <td>{v.anno}</td>
                                            <td>{v.targa}</td>
                                            <td>{v.vin}</td>
                                            <td>
                                                <select
                                                    className="adminveh-table-select"
                                                    value={v.stato}
                                                    onChange={(e) =>
                                                        handleChangeStato(
                                                            v.id,
                                                            e.target
                                                                .value as StatoVeicolo,
                                                        )
                                                    }
                                                >
                                                    {stati.map((s) => (
                                                        <option
                                                            key={s}
                                                            value={s}
                                                        >
                                                            {s}
                                                        </option>
                                                    ))}
                                                </select>
                                            </td>
                                            <td>
                                                    <span
                                                        className={
                                                            v.visibileAlPubblico
                                                                ? 'adminveh-badge adminveh-badge--visible'
                                                                : 'adminveh-badge adminveh-badge--hidden'
                                                        }
                                                    >
                                                        {v.visibileAlPubblico
                                                            ? 'Sì'
                                                            : 'No'}
                                                    </span>
                                            </td>
                                            <td>
                                                <div className="adminveh-table-actions">
                                                    <button
                                                        type="button"
                                                        onClick={() =>
                                                            startEdit(v)
                                                        }
                                                        className="adminveh-table-btn"
                                                    >
                                                        Modifica
                                                    </button>
                                                    <button
                                                        type="button"
                                                        onClick={() =>
                                                            handleDuplicate(
                                                                v.id,
                                                            )
                                                        }
                                                        className="adminveh-table-btn"
                                                    >
                                                        Duplica
                                                    </button>
                                                    <button
                                                        type="button"
                                                        onClick={() =>
                                                            handleDelete(
                                                                v.id,
                                                            )
                                                        }
                                                        className="adminveh-table-btn adminveh-table-btn--danger"
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

export default VehicleListPage