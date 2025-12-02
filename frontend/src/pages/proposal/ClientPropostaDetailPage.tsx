import { useEffect, useState } from 'react'
import { useNavigate, useParams, Link } from 'react-router-dom'

import {
    getProposta,
    acceptProposta,
    rejectProposta,
} from '../../services/proposalService'
import { getConfigurazioneById } from '../../services/configurationService'
import { cambiaStatoVeicolo, getVeicoloById } from '../../services/vehicleService'
import { getAllOptional } from '../../services/optionalService'

import type { PropostaDTO } from '../../entities/proposal'
import type { ConfigurazioneDTO } from '../../entities/configuration'
import type { VeicoloDTO } from '../../entities/vehicle'
import type { OptionalAccessorioDTO } from '../../entities/optional'

import './propostadetail.css'

const ClientPropostaDetailPage: React.FC = () => {
    const { id } = useParams<{ id: string }>()
    const navigate = useNavigate()

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

    useEffect(() => {
        if (!id) return

            ;(async () => {
            setLoading(true)
            setError(null)
            try {
                // Carico la proposta
                const p = await getProposta(Number(id))
                setProposta(p)

                // Carico la configurazione associata
                const cfg = await getConfigurazioneById(p.configurazioneId)
                setConfigurazione(cfg)

                // Carico veicolo e catalogo optional in parallelo
                const [v, tuttiOptional] = await Promise.all([
                    getVeicoloById(cfg.veicoloId),
                    getAllOptional(),
                ])
                setVeicolo(v)

                // filtro solo gli optional presenti nella configurazione
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
    }, [id])

    async function handleConfirm() {
        if (!proposta || !configurazione) return
        setSaving(true)
        setError(null)
        setInfo(null)
        try {
            // ATTENZIONE: lato service acceptProposta porta lo stato a COMPLETATA
            const updated = await acceptProposta(proposta.id)
            setProposta(updated)
            await cambiaStatoVeicolo(configurazione.veicoloId, 'VENDUTO')
            setInfo('Proposta confermata. Lo stato è ora COMPLETATA.')
        } catch (e) {
            console.error(e)
            setError('Errore durante la conferma della proposta.')
        } finally {
            setSaving(false)
        }
    }

    async function handleReject() {
        if (!proposta) return
        setSaving(true)
        setError(null)
        setInfo(null)
        try {
            const updated = await rejectProposta(proposta.id)
            setProposta(updated)
            setInfo('Proposta rifiutata.')
        } catch (e) {
            console.error(e)
            setError('Errore durante il rifiuto della proposta.')
        } finally {
            setSaving(false)
        }
    }

    if (loading) return <p>Caricamento…</p>
    if (!proposta) return <p>Proposta non trovata.</p>

    // regole di business lato cliente
    const canConfirm = proposta.stato === 'ACCETTATA'
    const canReject =
        proposta.stato === 'INVIATA' || proposta.stato === 'ACCETTATA'
    const isCompleted = proposta.stato === 'COMPLETATA'

    // fallback per i prezzi
    const prezzoBase =
        configurazione?.prezzoBase ??
        veicolo?.prezzoBase ??
        proposta.prezzoProposta
    const prezzoTotale =
        configurazione?.prezzoTotale ?? proposta.prezzoProposta
    const extraOptional = prezzoTotale - prezzoBase

    return (
        <div className="prop-page">
            <div className="prop-inner">
                {/* HEADER + BACK */}
                <header className="prop-header">
                    <div>
                        <h1 className="prop-title">
                            Dettaglio proposta #{proposta.id}
                        </h1>
                        <p className="prop-subtitle">
                            Rivedi i dettagli della proposta, della
                            configurazione e del veicolo prima di confermare o
                            rifiutare.
                        </p>
                    </div>

                    <button
                        type="button"
                        className="prop-back-btn"
                        onClick={() => navigate('/cliente/proposte')}
                    >
                        ← Torna alle mie proposte
                    </button>
                </header>

                {/* MESSAGGI */}
                {error && (
                    <p
                        className="prop-message prop-message--error"
                        style={{ color: 'red' }} // manteniamo lo stile rosso
                    >
                        {error}
                    </p>
                )}
                {info && (
                    <p
                        className="prop-message prop-message--info"
                        style={{ color: 'green' }} // manteniamo il verde
                    >
                        {info}
                    </p>
                )}

                {/* LAYOUT CARD */}
                <main className="prop-layout">
                    {/* PROPOSTA */}
                    <section className="prop-card prop-card--wide">
                        <h2 className="prop-card-title">Proposta commerciale</h2>
                        <div className="prop-grid">
                            <div className="prop-grid-item">
                                <span className="prop-label">
                                    Prezzo proposta
                                </span>
                                <span className="prop-value prop-value--main">
                                    {proposta.prezzoProposta.toLocaleString(
                                        'it-IT',
                                        {
                                            style: 'currency',
                                            currency: 'EUR',
                                        },
                                    )}
                                </span>
                            </div>
                            <div className="prop-grid-item">
                                <span className="prop-label">Stato</span>
                                <span className="prop-badge">
                                    {proposta.stato}
                                </span>
                            </div>
                            <div className="prop-grid-item">
                                <span className="prop-label">
                                    Data creazione
                                </span>
                                <span className="prop-value">
                                    {proposta.dataCreazione}
                                </span>
                            </div>
                            <div className="prop-grid-item">
                                <span className="prop-label">
                                    Data scadenza
                                </span>
                                <span className="prop-value">
                                    {proposta.dataScadenza ?? 'Non impostata'}
                                </span>
                            </div>
                            <div className="prop-grid-item prop-grid-item--full">
                                <span className="prop-label">
                                    Note cliente
                                </span>
                                <span className="prop-value">
                                    {proposta.noteCliente ?? '-'}
                                </span>
                            </div>
                            <div className="prop-grid-item prop-grid-item--full">
                                <span className="prop-label">
                                    Note interne
                                </span>
                                <span className="prop-value">
                                    {proposta.noteInterne ?? '-'}
                                </span>
                            </div>
                        </div>
                    </section>

                    {/* CONFIGURAZIONE */}
                    <section className="prop-card">
                        <h2 className="prop-card-title">
                            Configurazione veicolo
                        </h2>

                        {configurazione ? (
                            <div className="prop-config-body">
                                <p>
                                    <span className="prop-label-inline">
                                        ID configurazione:
                                    </span>{' '}
                                    <span className="prop-value-inline">
                                        {configurazione.id}
                                    </span>
                                </p>
                                <p>
                                    <span className="prop-label-inline">
                                        Prezzo base:
                                    </span>{' '}
                                    <span className="prop-value-inline">
                                        {prezzoBase.toLocaleString('it-IT', {
                                            style: 'currency',
                                            currency: 'EUR',
                                        })}
                                    </span>
                                </p>
                                <p>
                                    <span className="prop-label-inline">
                                        Totale configurazione:
                                    </span>{' '}
                                    <span className="prop-value-inline prop-value-inline--strong">
                                        {prezzoTotale.toLocaleString('it-IT', {
                                            style: 'currency',
                                            currency: 'EUR',
                                        })}
                                    </span>
                                </p>
                                <p>
                                    <span className="prop-label-inline">
                                        Extra optional:
                                    </span>{' '}
                                    <span className="prop-value-inline">
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
                                    <span className="prop-label-inline">
                                        Note configurazione:
                                    </span>{' '}
                                    <span className="prop-value-inline">
                                        {configurazione.note ?? '-'}
                                    </span>
                                </p>
                            </div>
                        ) : (
                            <p>Dati configurazione non disponibili.</p>
                        )}
                    </section>

                    {/* VEICOLO */}
                    <section className="prop-card">
                        <h2 className="prop-card-title">Veicolo</h2>
                        {veicolo ? (
                            <div className="prop-config-body">
                                <p className="prop-vehicle-main">
                                    <strong>{veicolo.marca}</strong>{' '}
                                    {veicolo.modello} ({veicolo.anno})
                                </p>
                                <p>
                                    <span className="prop-label-inline">
                                        Alimentazione:
                                    </span>{' '}
                                    <span className="prop-value-inline">
                                        {veicolo.alimentazione}
                                    </span>
                                </p>
                                <p>
                                    <span className="prop-label-inline">
                                        Cambio:
                                    </span>{' '}
                                    <span className="prop-value-inline">
                                        {veicolo.cambio}
                                    </span>
                                </p>
                                <p>
                                    <span className="prop-label-inline">
                                        Chilometraggio:
                                    </span>{' '}
                                    <span className="prop-value-inline">
                                        {veicolo.chilometraggio.toLocaleString(
                                            'it-IT',
                                        )}{' '}
                                        km
                                    </span>
                                </p>
                                <p>
                                    <span className="prop-label-inline">
                                        Prezzo di listino:
                                    </span>{' '}
                                    <span className="prop-value-inline">
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

                    {/* OPTIONAL */}
                    <section className="prop-card prop-card--wide">
                        <h2 className="prop-card-title">
                            Optional e accessori inclusi
                        </h2>
                        {optionalSelezionati.length === 0 ? (
                            <p>Nessun optional selezionato.</p>
                        ) : (
                            <ul className="prop-optional-list">
                                {optionalSelezionati.map((opt) => (
                                    <li key={opt.id} className="prop-optional-item">
                                        <div className="prop-optional-main">
                                            <strong>{opt.nome}</strong>
                                            {opt.prezzo != null && (
                                                <span className="prop-optional-price">
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
                                            <div className="prop-optional-desc">
                                                {opt.descrizione}
                                            </div>
                                        )}
                                    </li>
                                ))}
                            </ul>
                        )}
                    </section>

                    {/* AZIONI CLIENTE */}
                    <section className="prop-card prop-card--wide">
                        <h2 className="prop-card-title">Azione sulla proposta</h2>

                        {isCompleted && (
                            <p className="prop-info-text">
                                La proposta è già <b>COMPLETATA</b>. Non puoi
                                più modificarla.
                            </p>
                        )}

                        {!isCompleted && (
                            <>
                                {!canConfirm && !canReject && (
                                    <p className="prop-info-text">
                                        Puoi confermare solo se la proposta è
                                        nello stato <b>ACCETTATA</b>. Puoi
                                        rifiutare quando è <b>INVIATA</b> o{' '}
                                        <b>ACCETTATA</b>.
                                    </p>
                                )}

                                <div className="prop-actions">
                                    <button
                                        className="prop-primary-btn"
                                        onClick={handleConfirm}
                                        disabled={!canConfirm || saving}
                                    >
                                        Conferma proposta
                                    </button>
                                    <button
                                        className="prop-secondary-btn"
                                        onClick={handleReject}
                                        disabled={!canReject || saving}
                                    >
                                        Rifiuta proposta
                                    </button>
                                </div>
                            </>
                        )}

                        <p className="prop-footer-link">
                            <Link to="/cliente/proposte">
                                Torna all’elenco proposte
                            </Link>
                        </p>
                    </section>
                </main>
            </div>
        </div>
    )
}

export default ClientPropostaDetailPage