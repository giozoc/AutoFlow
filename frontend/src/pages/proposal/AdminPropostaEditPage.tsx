// src/pages/proposal/AdminPropostaEditPage.tsx

import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { getProposta, updateProposta } from '../../services/proposalService'
import { getConfigurazioneById } from '../../services/configurationService'
import { getVeicoloById } from '../../services/vehicleService'
import { getAllOptional } from '../../services/optionalService'
import { generaFatturaDaProposta } from '../../services/fatturaService'

import { getAuthState } from '../../services/authService'

import type { PropostaDTO, StatoProposta } from '../../entities/proposal'
import type { ConfigurazioneDTO } from '../../entities/configuration'
import type { VeicoloDTO } from '../../entities/vehicle'
import type { OptionalAccessorioDTO } from '../../entities/optional'
import type { FatturaDTO } from '../../entities/fattura'

import './adminpropostaedit.css'

const STATI_POSSIBILI: StatoProposta[] = [
    'BOZZA',
    'INVIATA',
    'ACCETTATA',
    'RIFIUTATA',
    'SCADUTA',
    'ANNULLATA',
    'COMPLETATA',
]

const AdminPropostaEditPage: React.FC = () => {
    const { id } = useParams<{ id: string }>()
    const navigate = useNavigate()

    const auth = getAuthState()
    const isAddettoVendite = auth.ruolo === 'ADDETTO_VENDITE'

    const [proposta, setProposta] = useState<PropostaDTO | null>(null)
    const [configurazione, setConfigurazione] =
        useState<ConfigurazioneDTO | null>(null)
    const [veicolo, setVeicolo] = useState<VeicoloDTO | null>(null)
    const [optionalSelezionati, setOptionalSelezionati] = useState<
        OptionalAccessorioDTO[]
    >([])

    const [loading, setLoading] = useState(true)
    const [saving, setSaving] = useState(false)
    const [error, setError] = useState<string | null>(null)
    const [info, setInfo] = useState<string | null>(null)

    const [fattura, setFattura] = useState<FatturaDTO | null>(null)
    const [generatingInvoice, setGeneratingInvoice] = useState(false)

    useEffect(() => {
        if (!id) return

            ;(async () => {
            setLoading(true)
            setError(null)
            try {
                // 1) Carico proposta
                const p = await getProposta(Number(id))

                // 2) Se sono un ADDETTO_VENDITE e la proposta non ha ancora un addetto,
                //    assegno automaticamente me stesso lato FE
                let propostaCaricata: PropostaDTO = p
                if (isAddettoVendite && !p.addettoVenditeId && auth.userId) {
                    propostaCaricata = {
                        ...p,
                        addettoVenditeId: auth.userId,
                    }
                }
                setProposta(propostaCaricata)

                // 3) Configurazione collegata
                const cfg = await getConfigurazioneById(p.configurazioneId)
                setConfigurazione(cfg)

                // 4) Veicolo + optional
                const [v, tuttiOptional] = await Promise.all([
                    getVeicoloById(cfg.veicoloId),
                    getAllOptional(),
                ])
                setVeicolo(v)

                const selected = tuttiOptional.filter((opt) =>
                    cfg.optionalIds?.includes(opt.id ?? -1),
                )
                setOptionalSelezionati(selected)
            } catch (e) {
                console.error(e)
                setError('Errore nel caricamento dei dati della proposta.')
            } finally {
                setLoading(false)
            }
        })()
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [id])

    function handleChange<K extends keyof PropostaDTO>(
        field: K,
        value: PropostaDTO[K],
    ) {
        if (!proposta) return
        setProposta({ ...proposta, [field]: value })
    }

    async function handleSave() {
        if (!proposta) return
        setSaving(true)
        setError(null)
        setInfo(null)
        try {
            const updated = await updateProposta(proposta.id, proposta)
            setProposta(updated)
            setInfo('Proposta aggiornata con successo.')
        } catch (e) {
            console.error(e)
            setError('Errore durante il salvataggio della proposta.')
        } finally {
            setSaving(false)
        }
    }

    async function handleGeneraFattura() {
        if (!proposta) return
        setGeneratingInvoice(true)
        setError(null)
        setInfo(null)
        try {
            const f = await generaFatturaDaProposta(proposta.id)
            setFattura(f)
            setInfo(`Fattura generata correttamente (n. ${f.numeroFattura}).`)
        } catch (e) {
            console.error(e)
            setError('Errore durante la generazione della fattura.')
        } finally {
            setGeneratingInvoice(false)
        }
    }

    if (loading) return <p>Caricamento…</p>
    if (!proposta) return <p>Proposta non trovata.</p>

    const prezzoBase =
        configurazione?.prezzoBase ??
        veicolo?.prezzoBase ??
        proposta.prezzoProposta
    const prezzoTotale =
        configurazione?.prezzoTotale ?? proposta.prezzoProposta
    const extraOptional = prezzoTotale - prezzoBase

    const puòGenerareFattura = proposta.stato === 'COMPLETATA'

    return (
        <div className="aprop-page">
            <div className="aprop-inner">
                {/* HEADER + BACK */}
                <header className="aprop-header">
                    <div>
                        <h1 className="aprop-title">
                            Modifica proposta #{proposta.id}
                        </h1>
                        <p className="aprop-subtitle">
                            Gestisci i dati della proposta, aggiorna lo stato e
                            genera la fattura a partire da una proposta
                            completata.
                        </p>
                    </div>

                    <button
                        type="button"
                        className="aprop-back-btn"
                        onClick={() => navigate('/admin/proposte')}
                    >
                        ← Torna alla gestione proposte
                    </button>
                </header>

                {/* MESSAGGI */}
                {error && (
                    <p
                        className="aprop-message aprop-message--error"
                        style={{ color: 'red' }} // lasciamo il rosso come prima
                    >
                        {error}
                    </p>
                )}
                {info && (
                    <p
                        className="aprop-message aprop-message--info"
                        style={{ color: 'green' }} // e il verde
                    >
                        {info}
                    </p>
                )}

                {/* LAYOUT PRINCIPALE */}
                <main className="aprop-layout">
                    {/* CARD: FORM PROPOSTA + FATTURA */}
                    <section className="aprop-card aprop-card--wide">
                        <h2 className="aprop-card-title">Dati proposta</h2>

                        <div className="aprop-form-grid">
                            <div className="aprop-form-group">
                                <label className="aprop-label">
                                    Cliente ID
                                </label>
                                <input
                                    type="number"
                                    value={proposta.clienteId}
                                    disabled
                                    className="aprop-input"
                                />
                            </div>

                            <div className="aprop-form-group">
                                <label className="aprop-label">
                                    Addetto vendite ID
                                </label>
                                <input
                                    type="number"
                                    value={proposta.addettoVenditeId ?? ''}
                                    readOnly
                                    disabled
                                    className="aprop-input"
                                />
                            </div>

                            <div className="aprop-form-group">
                                <label className="aprop-label">
                                    Configurazione ID
                                </label>
                                <input
                                    type="number"
                                    value={proposta.configurazioneId}
                                    disabled
                                    className="aprop-input"
                                />
                            </div>

                            <div className="aprop-form-group">
                                <label className="aprop-label">
                                    Prezzo proposta
                                </label>
                                <input
                                    type="number"
                                    value={proposta.prezzoProposta}
                                    disabled={puòGenerareFattura}
                                    onChange={(e) =>
                                        handleChange(
                                            'prezzoProposta',
                                            Number(e.target.value),
                                        )
                                    }
                                    className="aprop-input"
                                />
                            </div>

                            <div className="aprop-form-group">
                                <label className="aprop-label">Stato</label>
                                <select
                                    value={proposta.stato}
                                    onChange={(e) =>
                                        handleChange(
                                            'stato',
                                            e.target
                                                .value as StatoProposta,
                                        )
                                    }
                                    disabled={puòGenerareFattura}
                                    className="aprop-select"
                                >
                                    {STATI_POSSIBILI.map((s) => (
                                        <option key={s} value={s}>
                                            {s}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            <div className="aprop-form-group">
                                <label className="aprop-label">
                                    Data creazione
                                </label>
                                <input
                                    type="text"
                                    value={proposta.dataCreazione}
                                    disabled
                                    className="aprop-input"
                                />
                            </div>

                            <div className="aprop-form-group">
                                <label className="aprop-label">
                                    Data scadenza
                                </label>
                                <input
                                    type="date"
                                    value={proposta.dataScadenza ?? ''}
                                    onChange={(e) =>
                                        handleChange(
                                            'dataScadenza',
                                            e.target.value || null,
                                        )
                                    }
                                    className="aprop-input"
                                />
                            </div>

                            <div className="aprop-form-group aprop-form-group--full">
                                <label className="aprop-label">
                                    Note cliente
                                </label>
                                <textarea
                                    value={proposta.noteCliente ?? ''}
                                    readOnly
                                    className="aprop-textarea aprop-textarea--readonly"
                                />
                            </div>

                            <div className="aprop-form-group aprop-form-group--full">
                                <label className="aprop-label">
                                    Note interne
                                </label>
                                <textarea
                                    value={proposta.noteInterne ?? ''}
                                    onChange={(e) =>
                                        handleChange(
                                            'noteInterne',
                                            e.target.value,
                                        )
                                    }
                                    className="aprop-textarea"
                                />
                            </div>
                        </div>

                        <div className="aprop-actions-main">
                            <button
                                onClick={handleSave}
                                disabled={saving || puòGenerareFattura}
                                className="aprop-primary-btn"
                            >
                                {saving
                                    ? 'Salvataggio…'
                                    : 'Salva modifiche'}
                            </button>
                        </div>

                        {/* BLOCCO FATTURA */}
                        <div className="aprop-invoice-block">
                            <h3 className="aprop-invoice-title">
                                Contratto / Fattura
                            </h3>
                            {!puòGenerareFattura && (
                                <p className="aprop-invoice-info">
                                    La fattura è generabile solo quando la
                                    proposta è nello stato{' '}
                                    <b>COMPLETATA</b>.
                                </p>
                            )}

                            {puòGenerareFattura && (
                                <div className="aprop-invoice-actions">
                                    <button
                                        onClick={handleGeneraFattura}
                                        disabled={generatingInvoice}
                                        className="aprop-secondary-btn"
                                    >
                                        {generatingInvoice
                                            ? 'Generazione in corso…'
                                            : 'Genera contratto / fattura'}
                                    </button>

                                    {fattura && (
                                        <p className="aprop-invoice-result">
                                            Fattura generata: n.{' '}
                                            <b>{fattura.numeroFattura}</b> del{' '}
                                            {fattura.dataFattura} –{' '}
                                            {fattura.importoTotale.toLocaleString(
                                                'it-IT',
                                                {
                                                    style: 'currency',
                                                    currency: 'EUR',
                                                },
                                            )}
                                        </p>
                                    )}
                                </div>
                            )}
                        </div>
                    </section>

                    {/* CARD CONFIGURAZIONE */}
                    <section className="aprop-card">
                        <h2 className="aprop-card-title">
                            Configurazione collegata
                        </h2>

                        {configurazione ? (
                            <div className="aprop-info-body">
                                <p>
                                    <span className="aprop-label-inline">
                                        ID configurazione:
                                    </span>{' '}
                                    <span className="aprop-value-inline">
                                        {configurazione.id}
                                    </span>
                                </p>
                                <p>
                                    <span className="aprop-label-inline">
                                        Prezzo base:
                                    </span>{' '}
                                    <span className="aprop-value-inline">
                                        {prezzoBase.toLocaleString('it-IT', {
                                            style: 'currency',
                                            currency: 'EUR',
                                        })}
                                    </span>
                                </p>
                                <p>
                                    <span className="aprop-label-inline">
                                        Totale configurazione:
                                    </span>{' '}
                                    <span className="aprop-value-inline aprop-value-inline--strong">
                                        {prezzoTotale.toLocaleString('it-IT', {
                                            style: 'currency',
                                            currency: 'EUR',
                                        })}
                                    </span>
                                </p>
                                <p>
                                    <span className="aprop-label-inline">
                                        Extra optional:
                                    </span>{' '}
                                    <span className="aprop-value-inline">
                                        {extraOptional.toLocaleString(
                                            'it-IT',
                                            {
                                                style: 'currency',
                                                currency: 'EUR',
                                            },
                                        )}
                                    </span>
                                </p>
                                <p>
                                    <span className="aprop-label-inline">
                                        Note configurazione:
                                    </span>{' '}
                                    <span className="aprop-value-inline">
                                        {configurazione.note ?? '-'}
                                    </span>
                                </p>
                            </div>
                        ) : (
                            <p>Dati configurazione non disponibili.</p>
                        )}
                    </section>

                    {/* CARD VEICOLO */}
                    <section className="aprop-card">
                        <h2 className="aprop-card-title">Veicolo</h2>
                        {veicolo ? (
                            <div className="aprop-info-body">
                                <p className="aprop-vehicle-main">
                                    <strong>{veicolo.marca}</strong>{' '}
                                    {veicolo.modello} ({veicolo.anno})
                                </p>
                                <p>
                                    <span className="aprop-label-inline">
                                        Alimentazione:
                                    </span>{' '}
                                    <span className="aprop-value-inline">
                                        {veicolo.alimentazione}
                                    </span>
                                </p>
                                <p>
                                    <span className="aprop-label-inline">
                                        Cambio:
                                    </span>{' '}
                                    <span className="aprop-value-inline">
                                        {veicolo.cambio}
                                    </span>
                                </p>
                                <p>
                                    <span className="aprop-label-inline">
                                        Chilometraggio:
                                    </span>{' '}
                                    <span className="aprop-value-inline">
                                        {veicolo.chilometraggio.toLocaleString(
                                            'it-IT',
                                        )}{' '}
                                        km
                                    </span>
                                </p>
                                <p>
                                    <span className="aprop-label-inline">
                                        Prezzo di listino:
                                    </span>{' '}
                                    <span className="aprop-value-inline">
                                        {veicolo.prezzoBase.toLocaleString(
                                            'it-IT',
                                            {
                                                style: 'currency',
                                                currency: 'EUR',
                                            },
                                        )}
                                    </span>
                                </p>
                            </div>
                        ) : (
                            <p>Dati veicolo non disponibili.</p>
                        )}
                    </section>

                    {/* CARD OPTIONAL */}
                    <section className="aprop-card aprop-card--wide">
                        <h2 className="aprop-card-title">
                            Optional inclusi nella configurazione
                        </h2>
                        {optionalSelezionati.length === 0 ? (
                            <p>Nessun optional selezionato.</p>
                        ) : (
                            <ul className="aprop-optional-list">
                                {optionalSelezionati.map((opt) => (
                                    <li
                                        key={opt.id}
                                        className="aprop-optional-item"
                                    >
                                        <div className="aprop-optional-main">
                                            <strong>{opt.nome}</strong>
                                            {opt.prezzo != null && (
                                                <span className="aprop-optional-price">
                                                    {opt.prezzo.toLocaleString(
                                                        'it-IT',
                                                        {
                                                            style: 'currency',
                                                            currency: 'EUR',
                                                        },
                                                    )}
                                                </span>
                                            )}
                                        </div>
                                        {opt.descrizione && (
                                            <div className="aprop-optional-desc">
                                                {opt.descrizione}
                                            </div>
                                        )}
                                    </li>
                                ))}
                            </ul>
                        )}
                    </section>
                </main>
            </div>
        </div>
    )
}

export default AdminPropostaEditPage