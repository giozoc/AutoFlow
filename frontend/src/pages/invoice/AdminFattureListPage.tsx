// src/pages/fatture/AdminFattureListPage.tsx

import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'

import { getAllFatture, downloadFatturaPdf } from '../../services/fatturaService'
import type { FatturaDTO } from '../../entities/fattura'
import { getAuthState, logout } from '../../services/authService'

import './adminfatture.css'

const AdminFattureListPage: React.FC = () => {
    const [fatture, setFatture] = useState<FatturaDTO[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [downloadingId, setDownloadingId] = useState<number | null>(null)

    const auth = getAuthState()
    const navigate = useNavigate()
    const isAdmin = auth.ruolo === 'AMMINISTRATORE'

    useEffect(() => {
        ;(async () => {
            try {
                const data = await getAllFatture()
                setFatture(data)
            } catch (e) {
                console.error(e)
                setError('Errore nel caricamento delle fatture.')
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

    async function handleLogout() {
        await logout()
        navigate('/login', { replace: true })
    }

    return (
        <div className="adminfat-page">
            <div className="adminfat-inner">
                {/* NAVBAR ADMIN */}
                <header className="adminfat-header">
                    <div className="adminfat-logo-block">
                        <div className="adminfat-logo">
                            AutoFlow – <span>Area gestionale</span>
                        </div>
                        <p className="adminfat-subtitle">
                            Loggato come <strong>{auth.ruolo}</strong> (userId:{' '}
                            {auth.userId})
                        </p>
                    </div>

                    <div className="adminfat-header-right">
                        <nav className="adminfat-nav">
                            <Link
                                to="/"
                                className="showroom-header-link"
                            >
                                Profilo
                            </Link>

                            {isAdmin && (
                                <Link
                                    to="/admin/staff"
                                    className="adminfat-nav-link"
                                >
                                    Gestione addetti vendita
                                </Link>
                            )}

                            <Link
                                to="/admin/clienti"
                                className="adminfat-nav-link"
                            >
                                Gestione clienti
                            </Link>
                            <Link
                                to="/admin/veicoli"
                                className="adminfat-nav-link"
                            >
                                Gestione veicoli
                            </Link>
                            <Link
                                to="/admin/optional"
                                className="adminfat-nav-link"
                            >
                                Catalogo optional
                            </Link>
                            <Link
                                to="/admin/configurazioni"
                                className="adminfat-nav-link"
                            >
                                Configurazioni
                            </Link>
                            <Link
                                to="/admin/proposte"
                                className="adminfat-nav-link"
                            >
                                Gestione proposte
                            </Link>
                            <Link
                                to="/admin/fatture"
                                className="adminfat-nav-link adminfat-nav-link--active"
                            >
                                Fatture
                            </Link>
                            <Link
                                to="/admin/statistics"
                                className="adminfat-nav-link"
                            >
                                Statistiche
                            </Link>
                        </nav>

                        <button
                            className="adminfat-logout-btn"
                            onClick={handleLogout}
                        >
                            Logout
                        </button>
                    </div>
                </header>

                {/* TITOLO PAGINA */}
                <h1 className="adminfat-title">Gestione fatture</h1>

                {error && <p style={{ color: 'red' }}>{error}</p>}

                {/* LAYOUT SOLO TABELLA */}
                <main className="adminfat-layout">
                    <section className="adminfat-card adminfat-card--table">
                        <div className="adminfat-table-header">
                            <h2 className="adminfat-card-title">
                                Elenco fatture
                            </h2>
                            <p className="adminfat-table-subtitle">
                                {loading
                                    ? 'Caricamento...'
                                    : fatture.length === 0
                                        ? 'Nessuna fattura emessa.'
                                        : `${fatture.length} fatture trovate`}
                            </p>
                        </div>

                        {loading ? (
                            <p>Caricamento…</p>
                        ) : fatture.length === 0 ? (
                            <p>Nessuna fattura emessa.</p>
                        ) : (
                            <div className="adminfat-table-wrapper">
                                <table className="adminfat-table">
                                    <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>N. fattura</th>
                                        <th>Data</th>
                                        <th>Cliente</th>
                                        <th>Proposta</th>
                                        <th>Importo</th>
                                        <th>Note</th>
                                        <th>PDF</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {fatture.map((f) => (
                                        <tr key={f.id}>
                                            <td>{f.id}</td>
                                            <td>{f.numeroFattura}</td>
                                            <td>{f.dataFattura}</td>
                                            <td>{f.clienteId}</td>
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
                                                    className="adminfat-table-btn"
                                                    onClick={() =>
                                                        handleDownload(f)
                                                    }
                                                    disabled={
                                                        downloadingId ===
                                                        f.id
                                                    }
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
                        )}
                    </section>
                </main>
            </div>
        </div>
    )
}

export default AdminFattureListPage