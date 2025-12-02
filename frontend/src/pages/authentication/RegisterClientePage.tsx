import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import type { RegisterClienteDTO } from '../../entities/auth'
import { registerCliente } from '../../services/authService'

import './register.css'

const RegisterClientePage: React.FC = () => {
    const navigate = useNavigate()

    const [form, setForm] = useState<RegisterClienteDTO>({
        nome: '',
        cognome: '',
        email: '',
        password: '',
        telefono: '',
        indirizzo: '',
        codiceFiscale: '',
        dataNascita: '',
    })

    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)
    const [success, setSuccess] = useState<string | null>(null)

    function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
        const { name, value } = e.target
        setForm((prev) => ({ ...prev, [name]: value }))
    }

    async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault()
        setLoading(true)
        setError(null)
        setSuccess(null)

        try {
            await registerCliente(form)
            setSuccess(
                'Registrazione completata. Controlla la mail per attivare il tuo account.',
            )
            setTimeout(() => {
                navigate('/login')
            }, 1500)
        } catch (err: any) {
            console.error(err)
            if (err.response?.data?.message) {
                setError(err.response.data.message)
            } else {
                setError(
                    'Errore nella registrazione. Controlla i dati inseriti.',
                )
            }
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="reg-page">
            <div className="reg-card">
                <h1 className="reg-title">Registrazione cliente</h1>
                <p className="reg-subtitle">
                    Compila i campi per creare il tuo account <strong>AutoFlow</strong>.
                </p>

                {error && <div className="reg-alert reg-alert--error">{error}</div>}
                {success && (
                    <div className="reg-alert reg-alert--success">{success}</div>
                )}

                <form className="reg-form" onSubmit={handleSubmit}>
                    <div className="reg-group">
                        <label className="reg-label" htmlFor="nome">
                            Nome
                        </label>
                        <input
                            id="nome"
                            name="nome"
                            placeholder="Nome"
                            value={form.nome}
                            onChange={handleChange}
                            className="reg-input"
                            required
                        />
                    </div>

                    <div className="reg-group">
                        <label className="reg-label" htmlFor="cognome">
                            Cognome
                        </label>
                        <input
                            id="cognome"
                            name="cognome"
                            placeholder="Cognome"
                            value={form.cognome}
                            onChange={handleChange}
                            className="reg-input"
                            required
                        />
                    </div>

                    <div className="reg-group">
                        <label className="reg-label" htmlFor="email">
                            Email
                        </label>
                        <input
                            id="email"
                            name="email"
                            type="email"
                            placeholder="Email"
                            value={form.email}
                            onChange={handleChange}
                            className="reg-input"
                            required
                        />
                    </div>

                    <div className="reg-group">
                        <label className="reg-label" htmlFor="password">
                            Password
                        </label>
                        <input
                            id="password"
                            name="password"
                            type="password"
                            placeholder="Password"
                            value={form.password}
                            onChange={handleChange}
                            className="reg-input"
                            required
                        />
                    </div>

                    <div className="reg-group">
                        <label className="reg-label" htmlFor="telefono">
                            Telefono
                        </label>
                        <input
                            id="telefono"
                            name="telefono"
                            placeholder="Telefono"
                            value={form.telefono}
                            onChange={handleChange}
                            className="reg-input"
                            required
                        />
                    </div>

                    <div className="reg-group">
                        <label className="reg-label" htmlFor="indirizzo">
                            Indirizzo
                        </label>
                        <input
                            id="indirizzo"
                            name="indirizzo"
                            placeholder="Indirizzo"
                            value={form.indirizzo}
                            onChange={handleChange}
                            className="reg-input"
                            required
                        />
                    </div>

                    <div className="reg-group">
                        <label className="reg-label" htmlFor="codiceFiscale">
                            Codice fiscale
                        </label>
                        <input
                            id="codiceFiscale"
                            name="codiceFiscale"
                            placeholder="Codice fiscale"
                            value={form.codiceFiscale}
                            onChange={handleChange}
                            className="reg-input"
                            required
                        />
                    </div>

                    <div className="reg-group">
                        <label className="reg-label" htmlFor="dataNascita">
                            Data di nascita
                        </label>
                        <input
                            id="dataNascita"
                            type="date"
                            name="dataNascita"
                            value={form.dataNascita}
                            onChange={handleChange}
                            className="reg-input"
                            required
                        />
                    </div>

                    <button
                        type="submit"
                        className="reg-btn"
                        disabled={loading}
                    >
                        {loading ? 'Registrazione...' : 'Registrati'}
                    </button>
                </form>

                <p className="reg-footer">
                    Hai già un account?{' '}
                    <Link to="/login" className="reg-link">
                        Accedi
                    </Link>
                </p>
            </div>
        </div>
    )
}

export default RegisterClientePage