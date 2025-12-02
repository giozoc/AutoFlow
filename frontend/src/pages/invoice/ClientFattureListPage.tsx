// src/pages/fatture/ClientFattureListPage.tsx

import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { getFattureCliente, downloadFatturaPdf } from '../../services/fatturaService'
import type { FatturaDTO } from '../../entities/fattura'
import { getAuthState, logout } from '../../services/authService'

import './miefatture.css'

const ClientFattureListPage: React.FC = () => {
    const [fatture, setFatture] = useState<FatturaDTO[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [downloadingId, setDownloadingId] = useState<number | null>(null)

    const auth = getAuthState()
    const isClienteLoggato = !!auth.token && auth.ruolo === 'CLIENTE'
    const navigate = useNavigate()

    useEffect(() => {
        ;(async () => {
            try {
                const data = await getFattureCliente()
                setFatture(data)
            } catch (e) {
                console.error(e)
                // ⚠️ Deve rimanere questo messaggio
                setError('Errore nel caricamento delle tue fatture.')
            } finally {
                setLoading(false)
            }
        })()
    }, [])

    async function handleDownload(f: FatturaDTO) {
        try {
            setDownloadingId(f.id)
            const filename = f.numeroFattura
                ? `fattura-${f.numeroFattura}.pdf`
                : `fattura-${f.id}.pdf`

            await downloadFatturaPdf(f.id, filename)
        } catch (e) {
            console.error(e)
            alert('Errore durante il download del PDF.')
        } finally {
            setDownloadingId(null)
        }
    }

    return (
        <div className="miefatture-page">
            <div className="miefatture-inner">
                {/* NAVBAR */}
                <header className="miefatture-header">
                    <div className="miefatture-logo">
                        AutoFlow – <span>Le mie fatture</span>
                    </div>

                    <nav className="miefatture-header-right">
                        {isClienteLoggato ? (
                            <div className="miefatture-user-info">
                                <button
                                    className="miefatture-header-link"
                                    onClick={() => navigate('/')}
                                >
                                    Profilo
                                </button>

                                <button
                                    className="miefatture-header-link"
                                    onClick={() => navigate('/configurazioni/mie')}
                                >
                                    Le mie configurazioni
                                </button>

                                <button
                                    className="miefatture-header-link"
                                    onClick={() => navigate('/cliente/proposte')}
                                >
                                    Le mie proposte
                                </button>

                                <button
                                    className="miefatture-header-link miefatture-header-link--active"
                                    onClick={() => navigate('/cliente/fatture')}
                                >
                                    Le mie fatture
                                </button>

                                <button
                                    className="miefatture-logout-btn"
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
                                <Link className="miefatture-nav-link" to="/login">
                                    Login
                                </Link>
                                <Link className="miefatture-nav-link" to="/register">
                                    Registrati
                                </Link>
                            </>
                        )}
                    </nav>
                </header>

                {/* ⚠️ Messaggi invariati */}
                {loading && <p>Caricamento…</p>}
                {error && <p style={{ color: 'red' }}>{error}</p>}

                {!loading && !error && (
                    <>
                        {fatture.length === 0 ? (
                            <p>Non hai ancora fatture emesse.</p>
                        ) : (
                            <section className="miefatture-results">
                                <div className="miefatture-results-header">
                                    <h2 className="miefatture-results-title">
                                        Elenco fatture
                                    </h2>
                                    <p className="miefatture-results-subtitle">
                                        {fatture.length === 1
                                            ? '1 fattura trovata'
                                            : `${fatture.length} fatture trovate`}
                                    </p>
                                </div>

                                {/* TABELLA */}
                                <div className="miefatture-table-wrapper">
                                    <table className="miefatture-table">
                                        <thead>
                                        <tr>
                                            <th>N. fattura</th>
                                            <th>Data</th>
                                            <th>Proposta</th>
                                            <th>Importo</th>
                                            <th>Note</th>
                                            <th>PDF</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        {fatture.map((f) => (
                                            <tr key={f.id}>
                                                <td>{f.numeroFattura}</td>
                                                <td>{f.dataFattura}</td>
                                                <td>{f.propostaId}</td>
                                                <td>
                                                    {f.importoTotale.toLocaleString(
                                                        'it-IT',
                                                        {
                                                            style: 'currency',
                                                            currency: 'EUR',
                                                        },
                                                    )}
                                                </td>
                                                <td>{f.note ?? '-'}</td>
                                                <td>
                                                    <button
                                                        className="miefatture-btn-table"
                                                        onClick={() => handleDownload(f)}
                                                        disabled={downloadingId === f.id}
                                                    >
                                                        {downloadingId === f.id
                                                            ? 'Download...'
                                                            : 'Scarica PDF'}
                                                    </button>
                                                </td>
                                            </tr>
                                        ))}
                                        </tbody>
                                    </table>
                                </div>
                            </section>
                        )}
                    </>
                )}
            </div>
        </div>
    )
}

export default ClientFattureListPage