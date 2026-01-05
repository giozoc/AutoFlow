// src/pages/statistics/StatisticsPage.tsx

import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'

import '../../styles/adminstatistics.css'

import { getDashboardStatistics } from '../../services/statisticsService'
import type { DashboardStatisticsDTO } from '../../entities/statistics'
import { getAuthState, logout } from '../../services/authService'

const StatisticsPage: React.FC = () => {
    const [stats, setStats] = useState<DashboardStatisticsDTO | null>(null)
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const auth = getAuthState()
    const navigate = useNavigate()
    const isAdmin = auth.ruolo === 'AMMINISTRATORE'

    useEffect(() => {
        async function load() {
            setLoading(true)
            setError(null)
            try {
                const data = await getDashboardStatistics()
                setStats(data)
            } catch (e) {
                console.error(e)
                setError('Errore nel caricamento delle statistiche.')
            } finally {
                setLoading(false)
            }
        }

        void load()
    }, [])

    async function handleLogout() {
        await logout()
        navigate('/login', { replace: true })
    }

    return (
        <div className="adminstat-page">
            <div className="adminstat-inner">
                {/* NAVBAR ADMIN */}
                <header className="adminstat-header">
                    <div className="adminstat-logo-block">
                        <div className="adminstat-logo">
                            AutoFlow – <span>Area gestionale</span>
                        </div>
                        <p className="adminstat-subtitle">
                            Loggato come <strong>{auth.ruolo}</strong> (userId:{' '}
                            {auth.userId})
                        </p>
                    </div>

                    <div className="adminstat-header-right">
                        <nav className="adminstat-nav">
                            <Link to="/" className="showroom-header-link">
                                Profilo
                            </Link>

                            {isAdmin && (
                                <Link
                                    to="/admin/staff"
                                    className="adminstat-nav-link"
                                >
                                    Gestione addetti vendita
                                </Link>
                            )}

                            <Link
                                to="/admin/clienti"
                                className="adminstat-nav-link"
                            >
                                Gestione clienti
                            </Link>
                            <Link
                                to="/admin/veicoli"
                                className="adminstat-nav-link"
                            >
                                Gestione veicoli
                            </Link>
                            <Link
                                to="/admin/optional"
                                className="adminstat-nav-link"
                            >
                                Catalogo optional
                            </Link>
                            <Link
                                to="/admin/configurazioni"
                                className="adminstat-nav-link"
                            >
                                Configurazioni
                            </Link>
                            <Link
                                to="/admin/proposte"
                                className="adminstat-nav-link"
                            >
                                Gestione proposte
                            </Link>
                            <Link
                                to="/admin/fatture"
                                className="adminstat-nav-link"
                            >
                                Fatture
                            </Link>
                            {/* voce per questa pagina */}
                            <Link
                                to="/admin/statistiche"
                                className="adminstat-nav-link adminstat-nav-link--active"
                            >
                                Statistiche
                            </Link>
                        </nav>

                        <button
                            className="adminstat-logout-btn"
                            onClick={handleLogout}
                        >
                            Logout
                        </button>
                    </div>
                </header>

                {/* TITOLO PAGINA */}
                <h1 className="adminstat-title">Dashboard statistiche</h1>

                {error && <p style={{ color: 'red' }}>{error}</p>}

                {loading || !stats ? (
                    <p>Caricamento...</p>
                ) : (
                    <main className="adminstat-layout">
                        {/* CARD KPI principali */}
                        <section className="adminstat-card adminstat-card--kpi">
                            <h2 className="adminstat-card-title">
                                Riepilogo entità
                            </h2>
                            <div className="adminstat-kpi-grid">
                                <div className="adminstat-kpi">
                                    <span className="adminstat-kpi-label">
                                        Clienti
                                    </span>
                                    <span className="adminstat-kpi-value">
                                        {stats.totaleClienti}
                                    </span>
                                </div>
                                <div className="adminstat-kpi">
                                    <span className="adminstat-kpi-label">
                                        Veicoli a catalogo
                                    </span>
                                    <span className="adminstat-kpi-value">
                                        {stats.totaleVeicoli}
                                    </span>
                                </div>
                                <div className="adminstat-kpi">
                                    <span className="adminstat-kpi-label">
                                        Proposte totali
                                    </span>
                                    <span className="adminstat-kpi-value">
                                        {stats.totaleProposte}
                                    </span>
                                </div>
                                <div className="adminstat-kpi">
                                    <span className="adminstat-kpi-label">
                                        Fatture emesse
                                    </span>
                                    <span className="adminstat-kpi-value">
                                        {stats.totaleFatture}
                                    </span>
                                </div>
                            </div>
                        </section>

                        {/* CARD FATTURATO */}
                        <section className="adminstat-card adminstat-card--kpi">
                            <h2 className="adminstat-card-title">
                                Fatturato
                            </h2>
                            <div className="adminstat-kpi-grid">
                                <div className="adminstat-kpi">
                                    <span className="adminstat-kpi-label">
                                        Totale
                                    </span>
                                    <span className="adminstat-kpi-value">
                                        {stats.fatturatoTotale.toLocaleString(
                                            'it-IT',
                                            {
                                                style: 'currency',
                                                currency: 'EUR',
                                            },
                                        )}
                                    </span>
                                </div>
                                <div className="adminstat-kpi">
                                    <span className="adminstat-kpi-label">
                                        Anno corrente
                                    </span>
                                    <span className="adminstat-kpi-value">
                                        {stats.fatturatoAnnoCorrente.toLocaleString(
                                            'it-IT',
                                            {
                                                style: 'currency',
                                                currency: 'EUR',
                                            },
                                        )}
                                    </span>
                                </div>
                                <div className="adminstat-kpi">
                                    <span className="adminstat-kpi-label">
                                        Mese corrente
                                    </span>
                                    <span className="adminstat-kpi-value">
                                        {stats.fatturatoMeseCorrente.toLocaleString(
                                            'it-IT',
                                            {
                                                style: 'currency',
                                                currency: 'EUR',
                                            },
                                        )}
                                    </span>
                                </div>
                                <div className="adminstat-kpi">
                                    <span className="adminstat-kpi-label">
                                        Fatture non pagate
                                    </span>
                                    <span className="adminstat-kpi-value">
                                        {stats.fattureNonPagate}
                                    </span>
                                </div>
                            </div>
                        </section>

                        {/* CARD PROPOSTE PER STATO */}
                        <section className="adminstat-card adminstat-card--full">
                            <div className="adminstat-table-header">
                                <h2 className="adminstat-card-title">
                                    Proposte per stato
                                </h2>
                                <p className="adminstat-table-subtitle">
                                    Totale: {stats.totaleProposte}
                                </p>
                            </div>

                            <div className="adminstat-table-wrapper">
                                <table className="adminstat-table">
                                    <thead>
                                    <tr>
                                        <th>Stato</th>
                                        <th>Numero proposte</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <tr>
                                        <td>Bozza</td>
                                        <td>{stats.proposteBozza}</td>
                                    </tr>
                                    <tr>
                                        <td>Inviate</td>
                                        <td>{stats.proposteInviate}</td>
                                    </tr>
                                    <tr>
                                        <td>Accettate</td>
                                        <td>{stats.proposteAccettate}</td>
                                    </tr>
                                    <tr>
                                        <td>Rifiutate</td>
                                        <td>{stats.proposteRifiutate}</td>
                                    </tr>
                                    <tr>
                                        <td>Scadute</td>
                                        <td>{stats.proposteScadute}</td>
                                    </tr>
                                    <tr>
                                        <td>Annullate</td>
                                        <td>{stats.proposteAnnullate}</td>
                                    </tr>
                                    <tr>
                                        <td>Completate</td>
                                        <td>{stats.proposteCompletate}</td>
                                    </tr>
                                    </tbody>
                                </table>
                            </div>
                        </section>
                    </main>
                )}
            </div>
        </div>
    )
}

export default StatisticsPage