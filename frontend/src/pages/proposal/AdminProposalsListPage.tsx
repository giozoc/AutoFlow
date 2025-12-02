// src/pages/proposal/AdminProposalsListPage.tsx

import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { getTutteLeProposte } from '../../services/proposalService'
import type { PropostaDTO } from '../../entities/proposal'
import { getAuthState, logout } from '../../services/authService'

import './adminproposte.css'

const AdminProposalsListPage: React.FC = () => {
    const [proposte, setProposte] = useState<PropostaDTO[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    const auth = getAuthState()
    const navigate = useNavigate()
    const isAdmin = auth.ruolo === 'AMMINISTRATORE'

    useEffect(() => {
        ;(async () => {
            try {
                const data = await getTutteLeProposte()
                setProposte(data)
            } catch (e) {
                console.error(e)
                // 🔴 messaggio invariato
                setError('Errore nel caricamento delle proposte.')
            } finally {
                setLoading(false)
            }
        })()
    }, [])

    async function handleLogout() {
        await logout()
        navigate('/login', { replace: true })
    }

    return (
        <div className="adminprop-page">
            <div className="adminprop-inner">
                {/* NAVBAR ADMIN */}
                <header className="adminprop-header">
                    <div className="adminprop-logo-block">
                        <div className="adminprop-logo">
                            AutoFlow – <span>Area gestionale</span>
                        </div>
                        <p className="adminprop-subtitle">
                            Loggato come <strong>{auth.ruolo}</strong> (userId:{' '}
                            {auth.userId})
                        </p>
                    </div>

                    <div className="adminprop-header-right">
                        <nav className="adminprop-nav">
                            <Link
                                to="/"
                                className="showroom-header-link"
                            >
                                Profilo
                            </Link>
                            {isAdmin && (
                                <Link
                                    to="/admin/staff"
                                    className="adminprop-nav-link"
                                >
                                    Gestione addetti vendita
                                </Link>
                            )}

                            <Link
                                to="/admin/clienti"
                                className="adminprop-nav-link"
                            >
                                Gestione clienti
                            </Link>
                            <Link
                                to="/admin/veicoli"
                                className="adminprop-nav-link"
                            >
                                Gestione veicoli
                            </Link>
                            <Link
                                to="/admin/optional"
                                className="adminprop-nav-link"
                            >
                                Catalogo optional
                            </Link>
                            <Link
                                to="/admin/configurazioni"
                                className="adminprop-nav-link"
                            >
                                Configurazioni
                            </Link>
                            <Link
                                to="/admin/proposte"
                                className="adminprop-nav-link adminprop-nav-link--active"
                            >
                                Gestione proposte
                            </Link>
                            <Link
                                to="/admin/fatture"
                                className="adminprop-nav-link"
                            >
                                Fatture
                            </Link>
                            <Link
                                to="/admin/statistics"
                                className="adminprop-nav-link"
                            >
                                Statistiche
                            </Link>
                        </nav>

                        <button
                            className="adminprop-logout-btn"
                            onClick={handleLogout}
                        >
                            Logout
                        </button>
                    </div>
                </header>

                {/* TITOLO */}
                <h1 className="adminprop-title">Gestione proposte</h1>

                {error && <p style={{ color: 'red' }}>{error}</p>}

                <main className="adminprop-main">
                    <section className="adminprop-card">
                        <div className="adminprop-table-header">
                            <h2 className="adminprop-card-title">
                                Elenco proposte
                            </h2>
                            <p className="adminprop-table-subtitle">
                                {loading
                                    ? 'Caricamento…'
                                    : proposte.length === 0
                                        ? 'Nessuna proposta presente.'
                                        : `${proposte.length} proposte trovate`}
                            </p>
                        </div>

                        {loading ? (
                            <p>Caricamento…</p>
                        ) : proposte.length === 0 ? (
                            <p>Nessuna proposta presente.</p>
                        ) : (
                            <div className="adminprop-table-wrapper">
                                <table className="adminprop-table">
                                    <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Cliente</th>
                                        <th>Addetto</th>
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
                                            <td>{p.clienteId}</td>
                                            <td>
                                                {p.addettoVenditeId ??
                                                    '-' }
                                            </td>
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
                                            <td>{p.stato}</td>
                                            <td>
                                                <Link
                                                    to={`/admin/proposte/${p.id}`}
                                                    className="adminprop-btn-table"
                                                >
                                                    Apri / Modifica
                                                </Link>
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

export default AdminProposalsListPage