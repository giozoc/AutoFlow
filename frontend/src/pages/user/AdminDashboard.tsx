// src/pages/admin/AdminDashboard.tsx
import { Link, useNavigate } from 'react-router-dom'
import { getAuthState, logout } from '../../services/authService'

import '../../styles/admindashboard.css'

const AdminDashboard: React.FC = () => {
    const auth = getAuthState()
    const navigate = useNavigate()

    const isAdmin = auth.ruolo === 'AMMINISTRATORE'

    async function handleLogout() {
        await logout()
        navigate('/login', { replace: true })
    }

    return (
        <div className="admindash-page">
            <div className="admindash-inner">
                {/* HEADER + NAVBAR */}
                <header className="admindash-header">
                    <div className="admindash-logo-block">
                        <div className="admindash-logo">
                            AutoFlow – <span>Area gestionale</span>
                        </div>
                        <p className="admindash-subtitle">
                            Loggato come <strong>{auth.ruolo}</strong> (userId:{' '}
                            {auth.userId})
                        </p>
                    </div>

                    <div className="admindash-header-right">
                        <nav className="admindash-nav">
                            <Link
                                to="/"
                                className="showroom-header-link"
                            >
                                Profilo
                            </Link>
                            {isAdmin && (
                                <Link
                                    to="/admin/staff"
                                    className="admindash-nav-link"
                                >
                                    Gestione addetti vendita
                                </Link>
                            )}

                            <Link
                                to="/admin/clienti"
                                className="admindash-nav-link"
                            >
                                Gestione clienti
                            </Link>
                            <Link
                                to="/admin/veicoli"
                                className="admindash-nav-link"
                            >
                                Gestione veicoli
                            </Link>
                            <Link
                                to="/admin/optional"
                                className="admindash-nav-link"
                            >
                                Catalogo optional
                            </Link>
                            <Link
                                to="/admin/configurazioni"
                                className="admindash-nav-link"
                            >
                                Configurazioni
                            </Link>
                            <Link
                                to="/admin/proposte"
                                className="admindash-nav-link"
                            >
                                Gestione proposte
                            </Link>
                            <Link
                                to="/admin/fatture"
                                className="admindash-nav-link"
                            >
                                Fatture
                            </Link>
                            <Link
                                to="/admin/statistics"
                                className="admindash-nav-link"
                            >
                                Statistiche
                            </Link>
                        </nav>

                        <button
                            className="admindash-logout-btn"
                            onClick={handleLogout}
                        >
                            Logout
                        </button>
                    </div>
                </header>

                {/* BODY */}
                <main className="admindash-main">
                    <section className="admindash-card">
                        <h2 className="admindash-card-title">
                            Benvenuto nell’area gestionale
                        </h2>
                        <p className="admindash-card-text">
                            Seleziona una voce dal menu in alto per iniziare a
                            gestire clienti, veicoli, configurazioni, proposte o
                            fatture.
                        </p>
                    </section>
                </main>
            </div>
        </div>
    )
}

export default AdminDashboard