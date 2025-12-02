import { useEffect, useState, type FormEvent } from 'react'
import type { ConfigurazioneDTO } from '../../entities/configuration'
import type { VeicoloDTO } from '../../entities/vehicle'
import type { OptionalAccessorioDTO } from '../../entities/optional'
import type { PropostaDTO } from '../../entities/proposal'

import { getAllVeicoli } from '../../services/vehicleService'
import { getAllOptional } from '../../services/optionalService'
import {
    getAllConfigurazioni,
    createConfigurazione,
    deleteConfigurazione,
} from '../../services/configurationService'
import { getTutteLeProposte } from '../../services/proposalService'
import { getAuthState, logout } from '../../services/authService'
import { useNavigate, Link } from 'react-router-dom'

import './mieconf.css'

type ConfigForm = ConfigurazioneDTO

function emptyConfig(clienteId: number): ConfigForm {
    return {
        clienteId,
        veicoloId: 0,
        prezzoBase: 0,
        prezzoTotale: 0,
        note: '',
        optionalIds: [],
    }
}

const ClientConfigurationPage: React.FC = () => {
    const auth = getAuthState()
    const clienteId = auth.userId ?? 0
    const isClienteLoggato = !!auth.token && auth.ruolo === 'CLIENTE'

    const [configurazioni, setConfigurazioni] = useState<ConfigurazioneDTO[]>([])
    const [veicoli, setVeicoli] = useState<VeicoloDTO[]>([])
    const [optionals, setOptionals] = useState<OptionalAccessorioDTO[]>([])
    const [proposte, setProposte] = useState<PropostaDTO[]>([])
    const [form, setForm] = useState<ConfigForm>(emptyConfig(clienteId))

    const [loading, setLoading] = useState(false)
    const [saving, setSaving] = useState(false)
    const [error, setError] = useState<string | null>(null)
    const navigate = useNavigate()

    useEffect(() => {
        if (!clienteId) return

            ;(async () => {
            setLoading(true)
            setError(null)
            try {
                const [v, o, cfg, prp] = await Promise.all([
                    getAllVeicoli(),
                    getAllOptional(),
                    getAllConfigurazioni(),
                    getTutteLeProposte(),
                ])

                setVeicoli(v)
                setOptionals(o)
                setProposte(prp)

                // solo le configurazioni di questo cliente
                setConfigurazioni(cfg.filter((c) => c.clienteId === clienteId))

                // inizializzo il form con il primo veicolo NON VENDUTO
                const firstAvailable = v.find((x) => x.stato !== 'VENDUTO')
                const veicoloId = firstAvailable?.id ?? 0
                const veicolo = firstAvailable

                setForm((prev) => ({
                    ...prev,
                    clienteId,
                    veicoloId,
                    prezzoBase: veicolo?.prezzoBase ?? 0,
                    prezzoTotale: veicolo?.prezzoBase ?? 0,
                }))
            } catch (e) {
                console.error(e)
                setError('Errore nel caricamento dati per configurazione cliente.')
            } finally {
                setLoading(false)
            }
        })()
    }, [clienteId])

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

    async function handleSubmit(e: FormEvent) {
        e.preventDefault()
        setSaving(true)
        setError(null)

        try {
            const finalForm = ricalcolaPrezzi({
                ...form,
                clienteId, // forziamo il cliente lato FE
            })

            const created = await createConfigurazione(finalForm)
            setConfigurazioni((prev) => [...prev, created])
            setForm(emptyConfig(clienteId))
        } catch (err) {
            console.error(err)
            // 👉 MESSAGGIO IDENTICO
            setError('Errore nella creazione della configurazione.')
        } finally {
            setSaving(false)
        }
    }

    // 🔍 controlla se esiste una proposta per una configurazione
    function hasPropostaForConfig(configId?: number): boolean {
        if (!configId) return false
        return proposte.some((p) => p.configurazioneId === configId)
    }

    // handler per eliminare una configurazione del cliente
    async function handleDeleteConfigurazione(id?: number) {
        if (!id) return
        const conferma = window.confirm(
            'Sei sicuro di voler eliminare questa configurazione?',
        )
        if (!conferma) return

        try {
            await deleteConfigurazione(id)
            setConfigurazioni((prev) => prev.filter((c) => c.id !== id))
        } catch (err) {
            console.error(err)
            alert('Errore durante l’eliminazione della configurazione.')
        }
    }

    // solo veicoli non venduti
    const availableVeicoli = veicoli.filter((v) => v.stato !== 'VENDUTO')
    const formDisabled = availableVeicoli.length === 0

    return (
        <div className="mieconf-page">
            <div className="mieconf-inner">
                {/* HEADER / NAVBAR */}
                <header className="mieconf-header">
                    <div className="mieconf-logo">
                        AutoFlow – <span>Le mie configurazioni</span>
                    </div>

                    <nav className="mieconf-header-right">
                        {isClienteLoggato ? (
                            <div className="mieconf-user-info">
                                <Link to="/" className="mieconf-header-link">
                                    Profilo
                                </Link>
                                <Link
                                    to="/configurazioni/mie"
                                    className="mieconf-header-link mieconf-header-link--active"
                                >
                                    Le mie configurazioni
                                </Link>
                                <Link
                                    to="/cliente/proposte"
                                    className="mieconf-header-link"
                                >
                                    Le mie proposte
                                </Link>
                                <Link
                                    to="/cliente/fatture"
                                    className="mieconf-header-link"
                                >
                                    Le mie fatture
                                </Link>
                                <button
                                    className="mieconf-logout-btn"
                                    onClick={async () => {
                                        await logout()
                                        window.location.href = '/showroom'
                                    }}
                                >
                                    Logout
                                </button>
                            </div>
                        ) : (
                            <>
                                <Link
                                    to="/login"
                                    className="mieconf-nav-link"
                                >
                                    Login
                                </Link>
                                <Link
                                    to="/register"
                                    className="mieconf-nav-link"
                                >
                                    Registrati
                                </Link>
                            </>
                        )}
                    </nav>
                </header>

                


                {error && (
                    <p className="mieconf-message mieconf-message--error">
                        {error}
                    </p>
                )}
                {loading && (
                    <p className="mieconf-message">Caricamento dati...</p>
                )}

                {/* LAYOUT A DUE COLONNE: FORM + TABELLA */}
                <main className="mieconf-layout">
                    {/* FORM PANEL (tipo filtro showroom) */}
                    <section className="mieconf-form-panel">
                        <h2 className="mieconf-form-title">
                            Nuova configurazione
                        </h2>

                        {formDisabled ? (
                            <p className="mieconf-message mieconf-message--warning">
                                Nessun veicolo disponibile per la
                                configurazione.
                            </p>
                        ) : (
                            <form
                                onSubmit={handleSubmit}
                                className="mieconf-form"
                            >
                                {/* Veicolo */}
                                <div className="mieconf-form-group">
                                    <label
                                        htmlFor="veicoloId"
                                        className="mieconf-form-label"
                                    >
                                        Veicolo
                                    </label>
                                    <select
                                        id="veicoloId"
                                        name="veicoloId"
                                        value={form.veicoloId || ''}
                                        onChange={handleChangeSelect}
                                        required
                                        className="mieconf-select"
                                    >
                                        <option value="">
                                            Seleziona veicolo
                                        </option>
                                        {availableVeicoli.map((v) => (
                                            <option key={v.id} value={v.id}>
                                                {v.marca} {v.modello} ({v.anno})
                                            </option>
                                        ))}
                                    </select>
                                </div>

                                {/* Note */}
                                <div className="mieconf-form-group mieconf-form-group--full">
                                    <label
                                        htmlFor="note"
                                        className="mieconf-form-label"
                                    >
                                        Note
                                    </label>
                                    <textarea
                                        id="note"
                                        value={form.note ?? ''}
                                        onChange={handleNoteChange}
                                        placeholder="Note (facoltative)"
                                        className="mieconf-textarea"
                                    />
                                </div>

                                {/* Optional */}
                                <div className="mieconf-form-group mieconf-form-group--full">
                                    <h3 className="mieconf-optional-title">
                                        Optional / accessori
                                    </h3>
                                    {optionals.length === 0 ? (
                                        <p className="mieconf-message">
                                            Nessun optional disponibile.
                                        </p>
                                    ) : (
                                        <div className="mieconf-optional-grid">
                                            {optionals.map((o) => (
                                                <label
                                                    key={o.id}
                                                    className="mieconf-optional-card"
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
                                                    <span className="mieconf-optional-desc">
                                                        {o.descrizione}
                                                    </span>
                                                    <br />
                                                    <span className="mieconf-optional-price">
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
                                <div className="mieconf-form-group">
                                    <label className="mieconf-form-label">
                                        Prezzo base
                                    </label>
                                    <input
                                        type="number"
                                        value={form.prezzoBase ?? 0}
                                        readOnly
                                        className="mieconf-input-readonly"
                                    />
                                </div>

                                <div className="mieconf-form-group">
                                    <label className="mieconf-form-label">
                                        Prezzo totale
                                    </label>
                                    <input
                                        type="number"
                                        value={form.prezzoTotale ?? 0}
                                        readOnly
                                        className="mieconf-input-readonly mieconf-input-strong"
                                    />
                                </div>

                                <div className="mieconf-form-group mieconf-form-group--full">
                                    <button
                                        type="submit"
                                        disabled={saving}
                                        className="mieconf-btn-primary"
                                    >
                                        {saving
                                            ? 'Creazione...'
                                            : 'Crea configurazione'}
                                    </button>
                                </div>
                            </form>
                        )}
                    </section>

                    {/* TABELLA STORICO CONFIGURAZIONI */}
                    <section className="mieconf-results">
                        <div className="mieconf-results-header">
                            <h2 className="mieconf-results-title">
                                Storico configurazioni
                            </h2>
                            <p className="mieconf-results-subtitle">
                                {configurazioni.length === 0
                                    ? 'Nessuna configurazione salvata.'
                                    : `${configurazioni.length} configurazioni trovate`}
                            </p>
                        </div>

                        {configurazioni.length === 0 ? (
                            // 👉 MESSAGGIO IDENTICO
                            <p className="mieconf-message">
                                Nessuna configurazione salvata.
                            </p>
                        ) : (
                            <div className="mieconf-table-wrapper">
                                <table className="mieconf-table">
                                    <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Veicolo</th>
                                        <th>Prezzo totale</th>
                                        <th>Data</th>
                                        <th>Azioni</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {configurazioni.map((cfg) => {
                                        const hasProposta =
                                            hasPropostaForConfig(cfg.id)

                                        return (
                                            <tr key={cfg.id}>
                                                <td>{cfg.id}</td>
                                                <td>{cfg.veicoloId}</td>
                                                <td>
                                                    {cfg.prezzoTotale?.toLocaleString(
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
                                                    <button
                                                        onClick={() =>
                                                            navigate(
                                                                `/configurazioni/${cfg.id}/riepilogo`,
                                                            )
                                                        }
                                                        className={
                                                            hasProposta
                                                                ? 'mieconf-btn-table mieconf-btn-table--success'
                                                                : 'mieconf-btn-table mieconf-btn-table--primary'
                                                        }
                                                    >
                                                        {hasProposta
                                                            ? 'Dettaglio'
                                                            : 'Crea proposta'}
                                                    </button>
                                                    <button
                                                        onClick={() =>
                                                            handleDeleteConfigurazione(
                                                                cfg.id,
                                                            )
                                                        }
                                                        className={
                                                            hasProposta
                                                                ? 'mieconf-btn-delete mieconf-btn-delete--disabled'
                                                                : 'mieconf-btn-delete'
                                                        }
                                                        disabled={
                                                            hasProposta
                                                        }
                                                        title={
                                                            hasProposta
                                                                ? 'Configurazione associata a una proposta: non eliminabile.'
                                                                : ''
                                                        }
                                                    >
                                                        Elimina
                                                    </button>
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

export default ClientConfigurationPage