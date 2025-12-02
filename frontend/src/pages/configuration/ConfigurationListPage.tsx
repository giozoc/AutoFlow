// src/pages/configuration/ConfigurationListPage.tsx
import { useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import type { ConfigurazioneDTO } from '../../entities/configuration'
import type { ClienteDTO } from '../../entities/client'
import type { VeicoloDTO } from '../../entities/vehicle'
import type { OptionalAccessorioDTO } from '../../entities/optional'
import type { PropostaDTO } from '../../entities/proposal'

import { getAllClienti } from '../../services/clientService'
import { getAllVeicoli } from '../../services/vehicleService'
import { getAllOptional } from '../../services/optionalService'
import {
    getAllConfigurazioni,
    createConfigurazione,
    updateConfigurazione,
    deleteConfigurazione,
} from '../../services/configurationService'
import { getTutteLeProposte } from '../../services/proposalService'
import { getAuthState, logout } from '../../services/authService'

import './adminconfig.css'

type ConfigForm = ConfigurazioneDTO

function emptyConfig(): ConfigForm {
    return {
        clienteId: 0,
        veicoloId: 0,
        prezzoBase: 0,
        prezzoTotale: 0,
        note: '',
        optionalIds: [],
    }
}

const ConfigurationListPage: React.FC = () => {
    const [configurazioni, setConfigurazioni] = useState<ConfigurazioneDTO[]>([])
    const [clienti, setClienti] = useState<ClienteDTO[]>([])
    const [veicoli, setVeicoli] = useState<VeicoloDTO[]>([])
    const [optionals, setOptionals] = useState<OptionalAccessorioDTO[]>([])
    const [proposte, setProposte] = useState<PropostaDTO[]>([])

    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)
    const [form, setForm] = useState<ConfigForm>(emptyConfig())
    const [saving, setSaving] = useState(false)
    const [editingId, setEditingId] = useState<number | null>(null)

    const auth = getAuthState()
    const navigate = useNavigate()
    const isAdmin = auth.ruolo === 'AMMINISTRATORE'

    // Carica dati di base
    useEffect(() => {
        ;(async () => {
            setLoading(true)
            setError(null)
            try {
                const [c, v, o, cfg, prp] = await Promise.all([
                    getAllClienti(),
                    getAllVeicoli(),
                    getAllOptional(),
                    getAllConfigurazioni(),
                    getTutteLeProposte(),
                ])
                setClienti(c)
                setVeicoli(v)
                setOptionals(o)
                setConfigurazioni(cfg)
                setProposte(prp)

                // inizializza form con primo cliente/veicolo se esistono
                setForm((prev) => {
                    const clienteId = c[0]?.id ?? 0
                    const veicoloId = v[0]?.id ?? 0
                    const veicolo = v.find((x) => x.id === veicoloId)

                    return {
                        ...prev,
                        clienteId,
                        veicoloId,
                        prezzoBase: veicolo?.prezzoBase ?? 0,
                        prezzoTotale: veicolo?.prezzoBase ?? 0,
                        optionalIds: [],
                    }
                })
            } catch (e) {
                console.error(e)
                // 🔴 messaggio invariato
                setError('Errore nel caricamento dei dati per la configurazione.')
            } finally {
                setLoading(false)
            }
        })()
    }, [])

    function ricalcolaPrezzi(nextForm: ConfigForm): ConfigForm {
        const veicolo = veicoli.find((v) => v.id === nextForm.veicoloId)
        const prezzoBase = veicolo?.prezzoBase ?? 0

        const selectedOptionals = optionals.filter((o) =>
            nextForm.optionalIds.includes(o.id ?? -1),
        )
        const extra = selectedOptionals.reduce(
            (sum, o) => sum + (o.prezzo ?? 0),
            0,
        )

        return {
            ...nextForm,
            prezzoBase,
            prezzoTotale: prezzoBase + extra,
        }
    }

    function hasPropostaForConfig(configId?: number): boolean {
        if (!configId) return false
        return proposte.some((p) => p.configurazioneId === configId)
    }

    function handleChangeSelect(e: React.ChangeEvent<HTMLSelectElement>) {
        const { name, value } = e.target
        let next: ConfigForm = {
            ...form,
            [name]: Number(value),
        }

        if (name === 'veicoloId') {
            next = ricalcolaPrezzi(next)
        }

        setForm(next)
    }

    function handleNoteChange(e: React.ChangeEvent<HTMLTextAreaElement>) {
        const { value } = e.target
        setForm((prev) => ({ ...prev, note: value }))
    }

    function handleToggleOptional(id: number) {
        let nextIds: number[]
        if (form.optionalIds.includes(id)) {
            nextIds = form.optionalIds.filter((x) => x !== id)
        } else {
            nextIds = [...form.optionalIds, id]
        }

        const nextForm = ricalcolaPrezzi({
            ...form,
            optionalIds: nextIds,
        })

        setForm(nextForm)
    }

    function startEdit(cfg: ConfigurazioneDTO) {
        const filled: ConfigForm = {
            ...cfg,
            optionalIds: cfg.optionalIds ?? [],
        }
        const withPrices = ricalcolaPrezzi(filled)
        setForm(withPrices)
        setEditingId(cfg.id ?? null)
    }

    function cancelEdit() {
        setEditingId(null)
        setForm(emptyConfig())
    }

    async function handleSubmit(e: FormEvent) {
        e.preventDefault()
        setSaving(true)
        setError(null)
        try {
            const finalForm = ricalcolaPrezzi(form)

            if (editingId != null) {
                const updated = await updateConfigurazione(editingId, finalForm)
                setConfigurazioni((prev) =>
                    prev.map((c) => (c.id === editingId ? updated : c)),
                )
            } else {
                const created = await createConfigurazione(finalForm)
                setConfigurazioni((prev) => [...prev, created])
            }

            setForm(emptyConfig())
            setEditingId(null)
        } catch (err) {
            console.error(err)
            setError(
                editingId != null
                    ? "Errore nell'aggiornamento della configurazione."
                    : "Errore nella creazione della configurazione.",
            )
        } finally {
            setSaving(false)
        }
    }

    async function handleDelete(id?: number) {
        if (!id) return
        if (!confirm('Eliminare questa configurazione?')) return
        try {
            const ok = await deleteConfigurazione(id)
            if (ok) {
                setConfigurazioni((prev) => prev.filter((c) => c.id !== id))
            }
        } catch (err) {
            console.error(err)
            alert("Errore durante l'eliminazione della configurazione.")
        }
    }

    function getClienteLabel(id: number) {
        const c = clienti.find((x) => x.id === id)
        if (!c) return `Cliente #${id}`
        return `${c.nome} ${c.cognome} (id ${c.id})`
    }

    function getVeicoloLabel(id: number) {
        const v = veicoli.find((x) => x.id === id)
        if (!v) return `Veicolo #${id}`
        return `${v.marca} ${v.modello} (${v.anno})`
    }

    const formDisabled = clienti.length === 0 || veicoli.length === 0

    async function handleLogout() {
        await logout()
        navigate('/login', { replace: true })
    }

    return (
        <div className="admincfg-page">
            <div className="admincfg-inner">
                {/* NAVBAR ADMIN */}
                <header className="admincfg-header">
                    <div className="admincfg-logo-block">
                        <div className="admincfg-logo">
                            AutoFlow – <span>Area gestionale</span>
                        </div>
                        <p className="admincfg-subtitle">
                            Loggato come <strong>{auth.ruolo}</strong> (userId:{' '}
                            {auth.userId})
                        </p>
                    </div>

                    <div className="admincfg-header-right">
                        <nav className="admincfg-nav">
                            <Link
                                to="/"
                                className="showroom-header-link"
                            >
                                Profilo
                            </Link>

                            {isAdmin && (

                                <Link
                                    to="/admin/staff"
                                    className="admincfg-nav-link"
                                >
                                    Gestione addetti vendita
                                </Link>
                            )}

                            <Link
                                to="/admin/clienti"
                                className="admincfg-nav-link"
                            >
                                Gestione clienti
                            </Link>
                            <Link
                                to="/admin/veicoli"
                                className="admincfg-nav-link"
                            >
                                Gestione veicoli
                            </Link>
                            <Link
                                to="/admin/optional"
                                className="admincfg-nav-link"
                            >
                                Catalogo optional
                            </Link>
                            <Link
                                to="/admin/configurazioni"
                                className="admincfg-nav-link admincfg-nav-link--active"
                            >
                                Configurazioni
                            </Link>
                            <Link
                                to="/admin/proposte"
                                className="admincfg-nav-link"
                            >
                                Gestione proposte
                            </Link>
                            <Link
                                to="/admin/fatture"
                                className="admincfg-nav-link"
                            >
                                Fatture
                            </Link>

                            <Link
                                to="/admin/statistics"
                                className="admincfg-nav-link"
                            >
                                Statistiche
                            </Link>
                        </nav>

                        <button
                            className="admincfg-logout-btn"
                            onClick={handleLogout}
                        >
                            Logout
                        </button>
                    </div>
                </header>

                {/* TITOLO */}
                <h1 className="admincfg-title">Configurazioni veicolo</h1>

                {error && <p style={{ color: 'red' }}>{error}</p>}
                {loading && <p>Caricamento dati...</p>}

                {/* LAYOUT FORM + LISTA */}
                <main className="admincfg-layout">
                    {/* FORM CONFIGURAZIONE */}
                    <section className="admincfg-card admincfg-card--form">
                        <h2 className="admincfg-card-title">
                            {editingId != null
                                ? 'Modifica configurazione'
                                : 'Nuova configurazione'}
                        </h2>

                        {formDisabled ? (
                            <p style={{ color: 'orange' }}>
                                Per creare una configurazione devi avere almeno
                                un cliente e un veicolo.
                            </p>
                        ) : (
                            <form
                                onSubmit={handleSubmit}
                                className="admincfg-form-grid"
                            >
                                {/* Cliente */}
                                <div className="admincfg-form-group">
                                    <label
                                        htmlFor="clienteId"
                                        className="admincfg-label"
                                    >
                                        Cliente
                                    </label>
                                    <select
                                        id="clienteId"
                                        name="clienteId"
                                        value={form.clienteId || ''}
                                        onChange={handleChangeSelect}
                                        required
                                        className="admincfg-input admincfg-select"
                                    >
                                        <option value="">
                                            Seleziona cliente
                                        </option>
                                        {clienti.map((c) => (
                                            <option
                                                key={c.id}
                                                value={c.id}
                                            >
                                                {c.nome} {c.cognome} (id {c.id})
                                            </option>
                                        ))}
                                    </select>
                                </div>

                                {/* Veicolo */}
                                <div className="admincfg-form-group">
                                    <label
                                        htmlFor="veicoloId"
                                        className="admincfg-label"
                                    >
                                        Veicolo
                                    </label>
                                    <select
                                        id="veicoloId"
                                        name="veicoloId"
                                        value={form.veicoloId || ''}
                                        onChange={handleChangeSelect}
                                        required
                                        className="admincfg-input admincfg-select"
                                    >
                                        <option value="">
                                            Seleziona veicolo
                                        </option>
                                        {veicoli
                                            .filter(
                                                (v) => v.stato !== 'VENDUTO',
                                            )
                                            .map((v) => (
                                                <option
                                                    key={v.id}
                                                    value={v.id}
                                                >
                                                    {v.marca} {v.modello} (
                                                    {v.anno})
                                                </option>
                                            ))}
                                    </select>
                                </div>

                                {/* Note */}
                                <div className="admincfg-form-group admincfg-form-group--full">
                                    <label
                                        htmlFor="note"
                                        className="admincfg-label"
                                    >
                                        Note
                                    </label>
                                    <textarea
                                        id="note"
                                        value={form.note ?? ''}
                                        onChange={handleNoteChange}
                                        placeholder="Note interne (max 500 caratteri)"
                                        className="admincfg-textarea"
                                    />
                                </div>

                                {/* Optional */}
                                <div className="admincfg-form-group admincfg-form-group--full">
                                    <h3 className="admincfg-subsection-title">
                                        Optional / accessori
                                    </h3>
                                    {optionals.length === 0 ? (
                                        <p>Nessun optional definito.</p>
                                    ) : (
                                        <div className="admincfg-optionals-grid">
                                            {optionals.map((o) => (
                                                <label
                                                    key={o.id}
                                                    className="admincfg-optional-card"
                                                >
                                                    <input
                                                        type="checkbox"
                                                        checked={form.optionalIds.includes(
                                                            o.id ?? -1,
                                                        )}
                                                        onChange={() =>
                                                            o.id &&
                                                            handleToggleOptional(
                                                                o.id,
                                                            )
                                                        }
                                                    />{' '}
                                                    <strong>{o.nome}</strong>
                                                    <br />
                                                    <span className="admincfg-optional-desc">
                                                        {o.descrizione}
                                                    </span>
                                                    <br />
                                                    <span className="admincfg-optional-price">
                                                        {o.prezzo.toLocaleString(
                                                            'it-IT',
                                                        )}{' '}
                                                        €
                                                    </span>
                                                </label>
                                            ))}
                                        </div>
                                    )}
                                </div>

                                {/* Prezzi */}
                                <div className="admincfg-form-group">
                                    <label className="admincfg-label">
                                        Prezzo base
                                    </label>
                                    <input
                                        type="number"
                                        value={form.prezzoBase ?? 0}
                                        readOnly
                                        className="admincfg-input"
                                    />
                                </div>

                                <div className="admincfg-form-group">
                                    <label className="admincfg-label">
                                        Prezzo totale
                                    </label>
                                    <input
                                        type="number"
                                        value={form.prezzoTotale ?? 0}
                                        readOnly
                                        className="admincfg-input admincfg-input--bold"
                                    />
                                </div>

                                <div className="admincfg-form-actions">
                                    <button
                                        type="submit"
                                        disabled={saving}
                                        className="admincfg-btn-primary"
                                    >
                                        {saving
                                            ? editingId != null
                                                ? 'Salvataggio...'
                                                : 'Creazione...'
                                            : editingId != null
                                                ? 'Salva modifiche'
                                                : 'Crea configurazione'}
                                    </button>

                                    {editingId != null && (
                                        <button
                                            type="button"
                                            onClick={cancelEdit}
                                            className="admincfg-btn-secondary"
                                        >
                                            Annulla modifica
                                        </button>
                                    )}
                                </div>
                            </form>
                        )}
                    </section>

                    {/* LISTA CONFIGURAZIONI */}
                    <section className="admincfg-card admincfg-card--table">
                        <div className="admincfg-table-header">
                            <h2 className="admincfg-card-title">
                                Elenco configurazioni
                            </h2>
                            <p className="admincfg-table-subtitle">
                                {configurazioni.length === 0
                                    ? 'Nessuna configurazione presente.'
                                    : `${configurazioni.length} configurazioni trovate`}
                            </p>
                        </div>

                        {configurazioni.length === 0 ? (
                            <p>Nessuna configurazione presente.</p>
                        ) : (
                            <div className="admincfg-table-wrapper">
                                <table className="admincfg-table">
                                    <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Cliente</th>
                                        <th>Veicolo</th>
                                        <th>Prezzo base</th>
                                        <th>Prezzo totale</th>
                                        <th>Data creazione</th>
                                        <th>Azioni</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {configurazioni.map((cfg) => {
                                        const disabled =
                                            hasPropostaForConfig(cfg.id)

                                        return (
                                            <tr key={cfg.id}>
                                                <td>{cfg.id}</td>
                                                <td>
                                                    {getClienteLabel(
                                                        cfg.clienteId,
                                                    )}
                                                </td>
                                                <td>
                                                    {getVeicoloLabel(
                                                        cfg.veicoloId,
                                                    )}
                                                </td>
                                                <td>
                                                    {cfg.prezzoBase.toLocaleString(
                                                        'it-IT',
                                                    )}{' '}
                                                    €
                                                </td>
                                                <td>
                                                    {cfg.prezzoTotale.toLocaleString(
                                                        'it-IT',
                                                    )}{' '}
                                                    €
                                                </td>
                                                <td>
                                                    {cfg.dataCreazione
                                                        ? new Date(
                                                            cfg.dataCreazione,
                                                        ).toLocaleString(
                                                            'it-IT',
                                                        )
                                                        : '-'}
                                                </td>

                                                <td>
                                                    <div className="admincfg-table-actions">
                                                        <button
                                                            type="button"
                                                            onClick={() =>
                                                                startEdit(
                                                                    cfg,
                                                                )
                                                            }
                                                            disabled={
                                                                disabled
                                                            }
                                                            title={
                                                                disabled
                                                                    ? 'Configurazione associata a una proposta: non modificabile.'
                                                                    : ''
                                                            }
                                                            className="admincfg-table-btn"
                                                        >
                                                            Modifica
                                                        </button>
                                                        <button
                                                            type="button"
                                                            onClick={() =>
                                                                handleDelete(
                                                                    cfg.id,
                                                                )
                                                            }
                                                            disabled={
                                                                disabled
                                                            }
                                                            style={{
                                                                cursor:
                                                                    disabled
                                                                        ? 'not-allowed'
                                                                        : 'pointer',
                                                            }}
                                                            title={
                                                                disabled
                                                                    ? 'Configurazione associata a una proposta: non eliminabile.'
                                                                    : ''
                                                            }
                                                            className="admincfg-table-btn admincfg-table-btn--danger"
                                                        >
                                                            Elimina
                                                        </button>
                                                        <button
                                                            type="button"
                                                            onClick={() =>
                                                                navigate(
                                                                    `/admin/configurazioni/${cfg.id}/riepilogo`,
                                                                )
                                                            }
                                                            // questo pulsante DEVE essere sempre attivo
                                                            className="admincfg-table-btn admincfg-table-btn--primarysoft"
                                                            style={{
                                                                backgroundColor:
                                                                    disabled
                                                                        ? '#e0ffe0'
                                                                        : undefined,
                                                            }}
                                                        >
                                                            {disabled
                                                                ? 'Dettaglio'
                                                                : 'Crea proposta'}
                                                        </button>
                                                    </div>
                                                </td>
                                            </tr>
                                        )
                                    })}
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

export default ConfigurationListPage