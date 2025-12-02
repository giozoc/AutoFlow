import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { getProposteCliente } from '../../services/proposalService'
import type { PropostaDTO } from '../../entities/proposal'
import { getAuthState, logout } from '../../services/authService'

import './mieproposte.css'

const ClientProposalsListPage: React.FC = () => {
    const [proposte, setProposte] = useState<PropostaDTO[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    const auth = getAuthState()
    const isClienteLoggato = !!auth.token && auth.ruolo === 'CLIENTE'
    const navigate = useNavigate()

    useEffect(() => {
        ;(async () => {
            try {
                const data = await getProposteCliente()
                setProposte(data)
            } catch (e) {
                console.error(e)
                // 🔴 MESSAGGIO DI ERRORE IDENTICO
                setError('Errore nel caricamento delle proposte.')
            } finally {
                setLoading(false)
            }
        })()
    }, [])

    return (
        <div className="mieproposte-page">
            <div className="mieproposte-inner">
                {/* NAVBAR / HEADER */}
                <header className="mieproposte-header">
                    <div className="mieproposte-logo">
                        AutoFlow – <span>Le mie proposte</span>
                    </div>

                    <nav className="mieproposte-header-right">
                        {isClienteLoggato ? (
                            <div className="mieproposte-user-info">
                                <button
                                    className="mieproposte-header-link-as-button"
                                    onClick={() => navigate('/')}
                                >
                                    Profilo
                                </button>
                                <button
                                    className="mieproposte-header-link-as-button"
                                    onClick={() =>
                                        navigate('/configurazioni/mie')
                                    }
                                >
                                    Le mie configurazioni
                                </button>
                                <button
                                    className="mieproposte-header-link-as-button mieproposte-header-link--active"
                                    onClick={() =>
                                        navigate('/cliente/proposte')
                                    }
                                >
                                    Le mie proposte
                                </button>
                                <button
                                    className="mieproposte-header-link-as-button"
                                    onClick={() =>
                                        navigate('/cliente/fatture')
                                    }
                                >
                                    Le mie fatture
                                </button>
                                <button
                                    className="mieproposte-logout-btn"
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
                                    className="mieproposte-nav-link"
                                >
                                    Login
                                </Link>
                                <Link
                                    to="/register"
                                    className="mieproposte-nav-link"
                                >
                                    Registrati
                                </Link>
                            </>
                        )}
                    </nav>
                </header>


                {/* 🔄 MESSAGGI GENERALI – lasciamo il JSX come da te */}
                {loading && <p>Caricamento…</p>}
                {error && <p style={{ color: 'red' }}>{error}</p>}

                {!loading && !error && (
                    <>
                        {proposte.length === 0 ? (
                            <p>Non hai ancora proposte.</p>
                        ) : (
                            <section className="mieproposte-results">
                                <div className="mieproposte-results-header">
                                    <h2 className="mieproposte-results-title">
                                        Elenco proposte
                                    </h2>
                                    <p className="mieproposte-results-subtitle">
                                        {proposte.length === 1
                                            ? '1 proposta trovata'
                                            : `${proposte.length} proposte trovate`}
                                    </p>
                                </div>

                                <div className="mieproposte-table-wrapper">
                                    <table className="mieproposte-table">
                                        <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>Config.</th>
                                            <th>Prezzo</th>
                                            <th>Stato</th>
                                            <th>Dettaglio</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        {proposte.map((p) => (
                                            <tr key={p.id}>
                                                <td>{p.id}</td>
                                                <td>{p.configurazioneId}</td>
                                                <td>
                                                    {p.prezzoProposta.toLocaleString(
                                                        'it-IT',
                                                        {
                                                            style: 'currency',
                                                            currency: 'EUR',
                                                        },
                                                    )}
                                                </td>
                                                <td>
                                                        <span className="mieproposte-status-badge">
                                                            {p.stato}
                                                        </span>
                                                </td>
                                                <td>
                                                    <Link
                                                        to={`/cliente/proposte/${p.id}`}
                                                        className="mieproposte-btn-table"
                                                    >
                                                        Apri
                                                    </Link>
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

export default ClientProposalsListPage