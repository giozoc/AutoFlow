// src/pages/configuration/ConfigurazioneRiepilogoPage.tsx
import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { getConfigurazioneById } from '../../services/configurationService'
import {
    getVeicoloById,
    cambiaStatoVeicolo,
} from '../../services/vehicleService'
import { createProposta, getTutteLeProposte } from '../../services/proposalService'
import { getAuthState } from '../../services/authService'

import type { ConfigurazioneDTO } from '../../entities/configuration'
import type { VeicoloDTO } from '../../entities/vehicle'

import '../../styles/configurazioneriepilogo.css'

const ConfigurazioneRiepilogoPage: React.FC = () => {
    const { id } = useParams<{ id: string }>() // id configurazione
    const navigate = useNavigate()

    const [configurazione, setConfigurazione] =
        useState<ConfigurazioneDTO | null>(null)
    const [veicolo, setVeicolo] = useState<VeicoloDTO | null>(null)
    const [noteCliente, setNoteCliente] = useState('')
    const [loading, setLoading] = useState(true)
    const [saving, setSaving] = useState(false)
    const [error, setError] = useState<string | null>(null)
    const [info, setInfo] = useState<string | null>(null)
    const [hasProposta, setHasProposta] = useState(false)

    const auth = getAuthState()
    const ruolo = auth.ruolo

    useEffect(() => {
        if (!id) return

            ;(async () => {
            setLoading(true)
            setError(null)
            try {
                const cfg = await getConfigurazioneById(Number(id))
                setConfigurazione(cfg)

                const v = await getVeicoloById(cfg.veicoloId)
                setVeicolo(v)

                // 🔍 controlla se esiste già almeno una proposta per questa configurazione
                const tutteProposte = await getTutteLeProposte()
                const esiste = tutteProposte.some(
                    (p) => p.configurazioneId === cfg.id,
                )
                setHasProposta(esiste)

                if (esiste) {
                    setInfo(
                        'Esiste già una proposta per questa configurazione.',
                    )
                }
            } catch (e) {
                console.error(e)
                setError('Errore nel caricamento della configurazione.')
            } finally {
                setLoading(false)
            }
        })()
    }, [id])

    function handleBack() {
        if (ruolo === 'CLIENTE') {
            navigate('/configurazioni/mie')
        } else if (ruolo === 'ADDETTO_VENDITE' || ruolo === 'AMMINISTRATORE') {
            navigate('/admin/configurazioni')
        } else {
            navigate(-1)
        }
    }

    async function handleRichiediProposta() {
        if (hasProposta) {
            setError('Esiste già una proposta per questa configurazione.')
            return
        }

        if (!configurazione || !veicolo) return

        if (configurazione.id == null || veicolo.id == null) {
            setError('Configurazione o veicolo non validi (ID mancante).')
            return
        }

        if (!auth.userId || !ruolo) {
            setError('Devi essere autenticato per creare una proposta.')
            return
        }

        // 🔐 Decidiamo chi è il cliente e chi è l’addetto in base al ruolo
        let clienteId: number | null = null
        let addettoVenditeId: number | null = null

        if (ruolo === 'CLIENTE') {
            clienteId = auth.userId
        } else if (ruolo === 'ADDETTO_VENDITE' || ruolo === 'AMMINISTRATORE') {
            if (!configurazione.clienteId) {
                setError(
                    'Questa configurazione non è associata ad alcun cliente.',
                )
                return
            }
            clienteId = configurazione.clienteId

            if (ruolo === 'ADDETTO_VENDITE') {
                addettoVenditeId = auth.userId
            } else {
                addettoVenditeId = null
            }
        } else {
            setError('Ruolo non autorizzato a creare proposte.')
            return
        }

        setSaving(true)
        setError(null)
        setInfo(null)

        try {
            await createProposta({
                clienteId,
                addettoVenditeId: addettoVenditeId ?? null,
                configurazioneId: configurazione.id,
                prezzoProposta: configurazione.prezzoTotale ?? veicolo.prezzoBase,
                noteCliente:
                    ruolo === 'CLIENTE' ? noteCliente || null : null,
            })

            // veicolo va in stato OPZIONATO
            await cambiaStatoVeicolo(veicolo.id, 'OPZIONATO')

            setInfo('Proposta creata e veicolo opzionato.')

            // redirect diverso in base al ruolo
            if (ruolo === 'CLIENTE') {
                navigate('/cliente/proposte')
            } else {
                navigate('/admin/proposte')
            }
        } catch (e) {
            console.error(e)
            setError('Errore durante la creazione della proposta.')
        } finally {
            setSaving(false)
        }
    }

    if (loading) return <p>Caricamento…</p>
    if (!configurazione || !veicolo)
        return <p>Configurazione non trovata.</p>

    const prezzoBase = veicolo.prezzoBase
    const prezzoTotale = configurazione.prezzoTotale ?? veicolo.prezzoBase
    const extraOptional = prezzoTotale - prezzoBase

    const isCliente = ruolo === 'CLIENTE'

    return (
        <div className="cfg-riep-page">
            <div className="cfg-riep-inner">
                {/* HEADER SEMPLICE + BACK BUTTON */}
                <header className="cfg-riep-header">
                    <div>
                        <h1 className="cfg-riep-title">
                            Riepilogo configurazione
                        </h1>
                        <p className="cfg-riep-subtitle">
                            Controlla i dettagli del veicolo e della
                            configurazione prima di creare una proposta
                            commerciale.
                        </p>
                    </div>

                    <button
                        type="button"
                        className="cfg-riep-back-btn"
                        onClick={handleBack}
                    >
                        ← Indietro
                    </button>
                </header>

                {/* MESSAGGI */}
                {error && (
                    <p className="cfg-riep-message cfg-riep-message--error">
                        {error}
                    </p>
                )}
                {info && (
                    <p className="cfg-riep-message cfg-riep-message--info">
                        {info}
                    </p>
                )}

                {/* LAYOUT PRINCIPALE: VEICOLO + CONFIGURAZIONE / NOTE */}
                <main className="cfg-riep-layout">
                    {/* CARD VEICOLO */}
                    <section className="cfg-riep-card">
                        <h2 className="cfg-riep-card-title">Veicolo</h2>
                        <p className="cfg-riep-vehicle-main">
                            <strong>{veicolo.marca}</strong> {veicolo.modello}{' '}
                            ({veicolo.anno})
                        </p>
                        <div className="cfg-riep-vehicle-details">
                            <p>
                                <span>Alimentazione:</span>{' '}
                                <strong>{veicolo.alimentazione}</strong>
                            </p>
                            <p>
                                <span>Cambio:</span>{' '}
                                <strong>{veicolo.cambio}</strong>
                            </p>
                            <p>
                                <span>Prezzo base:</span>{' '}
                                <strong>
                                    {prezzoBase.toLocaleString('it-IT', {
                                        style: 'currency',
                                        currency: 'EUR',
                                    })}
                                </strong>
                            </p>
                        </div>
                    </section>

                    {/* CARD CONFIG + NOTE + CTA */}
                    <section className="cfg-riep-card cfg-riep-card--wide">
                        <h2 className="cfg-riep-card-title">
                            Dettagli configurazione
                        </h2>

                        <div className="cfg-riep-config-grid">
                            <div className="cfg-riep-config-item">
                                <span className="cfg-riep-config-label">
                                    Totale configurazione
                                </span>
                                <span className="cfg-riep-config-value cfg-riep-config-value--main">
                                    {prezzoTotale.toLocaleString('it-IT', {
                                        style: 'currency',
                                        currency: 'EUR',
                                    })}
                                </span>
                            </div>
                            <div className="cfg-riep-config-item">
                                <span className="cfg-riep-config-label">
                                    Extra optional
                                </span>
                                <span className="cfg-riep-config-value">
                                    {extraOptional.toLocaleString('it-IT', {
                                        style: 'currency',
                                        currency: 'EUR',
                                    })}
                                </span>
                            </div>
                            <div className="cfg-riep-config-item">
                                <span className="cfg-riep-config-label">
                                    Note configurazione
                                </span>
                                <span className="cfg-riep-config-value">
                                    {configurazione.note ?? '-'}
                                </span>
                            </div>
                            {configurazione.clienteId && (
                                <div className="cfg-riep-config-item">
                                    <span className="cfg-riep-config-label">
                                        ID cliente
                                    </span>
                                    <span className="cfg-riep-config-value">
                                        {configurazione.clienteId}
                                    </span>
                                </div>
                            )}
                        </div>

                        {isCliente && (
                            <div className="cfg-riep-notes-block">
                                <h3 className="cfg-riep-notes-title">
                                    Note per l’addetto vendite
                                </h3>
                                <textarea
                                    className="cfg-riep-textarea"
                                    value={noteCliente}
                                    onChange={(e) =>
                                        setNoteCliente(e.target.value)
                                    }
                                    rows={4}
                                    placeholder="Scrivi qui eventuali richieste o preferenze aggiuntive..."
                                />
                            </div>
                        )}

                        <div className="cfg-riep-actions">
                            <button
                                type="button"
                                className="cfg-riep-primary-btn"
                                onClick={handleRichiediProposta}
                                disabled={saving || hasProposta}
                            >
                                {saving
                                    ? 'Invio in corso…'
                                    : 'Crea proposta commerciale'}
                            </button>

                            {hasProposta && (
                                <span className="cfg-riep-badge-existing">
                                    Proposta già esistente per questa
                                    configurazione
                                </span>
                            )}
                        </div>
                    </section>
                </main>
            </div>
        </div>
    )
}

export default ConfigurazioneRiepilogoPage