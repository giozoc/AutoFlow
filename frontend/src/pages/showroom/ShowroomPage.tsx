import { useEffect, useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import type { ShowroomFiltro } from '../../entities/showroom'
import type { VeicoloDTO } from '../../entities/vehicle'
import { searchShowroom } from '../../services/showroomService'
import { getAuthState, logout } from '../../services/authService'
import { getClienteById } from '../../services/clientService' // 👈 IMPORT CORRETTO

import './showroom.css'

function emptyFiltro(): ShowroomFiltro {
    return {
        marca: '',
        modello: '',
        prezzoMin: null,
        prezzoMax: null,
    }
}

const ShowroomPage: React.FC = () => {
    const [filtro, setFiltro] = useState<ShowroomFiltro>(emptyFiltro())
    const [veicoli, setVeicoli] = useState<VeicoloDTO[]>([])
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)
    const [nomeCliente, setNomeCliente] = useState<string | null>(null) // 👈 NEW

    const auth = getAuthState()
    const isClienteLoggato = !!auth.token && auth.ruolo === 'CLIENTE'

    async function load(f: ShowroomFiltro) {
        setLoading(true)
        setError(null)
        try {
            const data = await searchShowroom(f)
            setVeicoli(data)
        } catch (e) {
            console.error(e)
            setError('Errore nel caricamento dei veicoli.')
        } finally {
            setLoading(false)
        }
    }

    // carica veicoli
    useEffect(() => {
        void load(emptyFiltro())
    }, [])

    // carica nome cliente se loggato
    useEffect(() => {
        const id = auth.userId
        if (!isClienteLoggato || id == null) return

        void (async () => {
            try {
                const cliente = await getClienteById(id) // 👈 qui id è sicuramente number
                setNomeCliente(cliente.nome)
            } catch (e) {
                console.error('Errore caricamento nome cliente', e)
                setNomeCliente(null)
            }
        })()
    }, [isClienteLoggato, auth.userId])

    function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
        const { name, value } = e.target
        let newValue: string | number | null = value

        if (name === 'prezzoMin' || name === 'prezzoMax') {
            newValue = value === '' ? null : Number(value)
        }

        setFiltro((prev) => ({
            ...prev,
            [name]: newValue as never,
        }))
    }

    async function handleSubmit(e: FormEvent) {
        e.preventDefault()
        await load(filtro)
    }

    function handleReset() {
        const f = emptyFiltro()
        setFiltro(f)
        void load(f)
    }

    return (
        <div className="showroom-page">
            <div className="showroom-inner">
                {/* HEADER */}


                <header className="showroom-header">
                    <div className="showroom-logo">
                        AutoFlow – <span>Showroom</span>
                    </div>

                    <nav className="showroom-header-right">
                        {isClienteLoggato ? (
                            <div
                                className="showroom-user-info"
                                data-test="dashboard-cliente"
                            >
                <span className="showroom-user-welcome">
                    Ciao <strong>{nomeCliente ?? 'cliente'}</strong> 👋
                </span>
                                <Link to="/profilo" className="showroom-header-link">
                                    Profilo
                                </Link>
                                <Link
                                    to="/configurazioni/mie"
                                    className="showroom-header-link"
                                >
                                    Le mie configurazioni
                                </Link>
                                <Link
                                    to="/cliente/proposte"
                                    className="showroom-header-link"
                                >
                                    Le mie proposte
                                </Link>
                                <Link
                                    to="/cliente/fatture"
                                    className="showroom-header-link"
                                >
                                    Le mie fatture
                                </Link>
                                <button
                                    className="showroom-logout-btn"
                                    data-test="logout-button"
                                    onClick={async () => {
                                        await logout();
                                        window.location.href = '/showroom';
                                    }}
                                >
                                    Logout
                                </button>
                            </div>
                        ) : (
                            <>
                                <Link to="/login" className="showroom-nav-link">
                                    Login
                                </Link>
                                <Link to="/register" className="showroom-nav-link">
                                    Registrati
                                </Link>
                            </>
                        )}
                    </nav>
                </header>

                {/* LAYOUT: FILTRO + RISULTATI */}
                <main className="showroom-layout">
                    {/* FILTRO */}
                    <section className="showroom-filter-panel">
                        <h2 className="showroom-filter-title">Filtra veicoli</h2>

                        <form
                            onSubmit={handleSubmit}
                            className="showroom-filter-form"
                        >
                            <div className="showroom-filter-group">
                                <label
                                    htmlFor="marca"
                                    className="showroom-filter-label"
                                >
                                    Marca
                                </label>
                                <input
                                    id="marca"
                                    name="marca"
                                    className="showroom-filter-input"
                                    placeholder="Marca"
                                    value={filtro.marca ?? ''}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="showroom-filter-group">
                                <label
                                    htmlFor="modello"
                                    className="showroom-filter-label"
                                >
                                    Modello
                                </label>
                                <input
                                    id="modello"
                                    name="modello"
                                    className="showroom-filter-input"
                                    placeholder="Modello"
                                    value={filtro.modello ?? ''}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="showroom-filter-group">
                                <label
                                    htmlFor="prezzoMin"
                                    className="showroom-filter-label"
                                >
                                    Prezzo min
                                </label>
                                <input
                                    id="prezzoMin"
                                    name="prezzoMin"
                                    type="number"
                                    className="showroom-filter-input"
                                    placeholder="Prezzo min"
                                    value={filtro.prezzoMin ?? ''}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="showroom-filter-group">
                                <label
                                    htmlFor="prezzoMax"
                                    className="showroom-filter-label"
                                >
                                    Prezzo max
                                </label>
                                <input
                                    id="prezzoMax"
                                    name="prezzoMax"
                                    type="number"
                                    className="showroom-filter-input"
                                    placeholder="Prezzo max"
                                    value={filtro.prezzoMax ?? ''}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="showroom-filter-actions">
                                <button
                                    type="submit"
                                    className="showroom-btn-primary"
                                >
                                    Applica filtro
                                </button>
                                <button
                                    type="button"
                                    onClick={handleReset}
                                    className="showroom-btn-secondary"
                                >
                                    Reset
                                </button>
                            </div>
                        </form>
                    </section>

                    {/* RISULTATI */}
                    <section className="showroom-results">
                        <div className="showroom-results-header">
                            <h2 className="showroom-results-title">
                                Veicoli trovati
                            </h2>
                            <p className="showroom-results-subtitle">
                                {loading
                                    ? 'Caricamento in corso...'
                                    : veicoli.length > 0
                                        ? `${veicoli.length} veicoli trovati`
                                        : 'Nessun veicolo trovato'}
                            </p>
                        </div>

                        {error && (
                            <p className="showroom-message showroom-message--error">
                                {error}
                            </p>
                        )}

                        {loading ? (
                            <p className="showroom-message">Caricamento...</p>
                        ) : veicoli.length === 0 ? (
                            <p className="showroom-message">
                                Nessun veicolo trovato con questi criteri.
                            </p>
                        ) : (
                            <div className="showroom-cards-grid">
                                {veicoli.map((v) => (
                                    <div
                                        key={v.id}
                                        className="showroom-card"
                                    >
                                        <h3 className="showroom-card-title">
                                            {v.marca} {v.modello}
                                        </h3>
                                        <p className="showroom-card-info">
                                            Anno: <strong>{v.anno}</strong>
                                        </p>
                                        <p className="showroom-card-info">
                                            Prezzo base:{' '}
                                            {v.prezzoBase.toLocaleString(
                                                'it-IT',
                                            )}{' '}
                                            €
                                        </p>
                                        <p className="showroom-card-info">
                                            Chilometraggio:{' '}
                                            {v.chilometraggio.toLocaleString(
                                                'it-IT',
                                            )}{' '}
                                            km
                                        </p>
                                        <p className="showroom-card-info">
                                            Alimentazione: {v.alimentazione}
                                        </p>
                                        <p className="showroom-card-info">
                                            Cambio: {v.cambio}
                                        </p>
                                    </div>
                                ))}
                            </div>
                        )}
                    </section>
                </main>
            </div>
        </div>
    )
}

export default ShowroomPage