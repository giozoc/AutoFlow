// src/pages/authentication/LoginPage.tsx

import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { login } from '../../services/authService';

import '../../styles/login.css';

export default function LoginPage() {
    const navigate = useNavigate();
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();
        setError(null);

        //TC1_01 / TC1_02 / TC1_03 → request non valida
        if (!email || !password) {
            setError('Credenziali non valide');
            return;
        }

        setLoading(true);

        try {
            const auth = await login({ email, password });

            if (auth.mustChangePassword) {
                // TC1_07 – account non attivo ma password corretta
                navigate('/password/first-change', {
                    replace: true,
                    state: { email },
                });
                return;
            }

            // TC1_08 – login valido, account attivo
            if (auth.ruolo === 'AMMINISTRATORE' || auth.ruolo === 'ADDETTO_VENDITE') {
                navigate('/admin', { replace: true });
            } else {
                navigate('/showroom', { replace: true });
            }
        } catch (err: unknown) {
            console.error(err);

            let message = 'Credenziali non valide o errore di connessione.';

            if (err instanceof Error && err.message) {
                // Possibili messaggi dal backend:
                //  - "Credenziali non valide"
                //  - "Utente non trovato"
                //  - "Password errata"
                //  - "Account non attivo"
                switch (err.message) {
                    case 'Credenziali non valide':
                    case 'Utente non trovato':
                    case 'Password errata':
                    case 'Account non attivo':
                        message = err.message;
                        break;
                    default:
                        // tieni il fallback generico se non è uno di quelli
                        break;
                }
            }

            setError(message);
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="login-page" data-test="login-page">
            <form
                className="login-card"
                data-test="login-form"
                onSubmit={handleSubmit}
            >
                <h1 className="login-title">AutoFlow – Login</h1>

                <div className="login-group">
                    <label className="login-label" htmlFor="email">Email</label>
                    <input
                        id="email"
                        data-test="login-email"
                        type="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        className="login-input"
                    />
                </div>

                <div className="login-group">
                    <label className="login-label" htmlFor="password">Password</label>
                    <input
                        id="password"
                        data-test="login-password"
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        className="login-input"
                    />
                </div>

                <p className="login-forgot">
                    Hai dimenticato la password?{' '}
                    <Link to="/password/forgot">Recupera password</Link>
                </p>

                {error && (
                    <div
                        className="login-error"
                        data-test="login-error"
                    >
                        {error}
                    </div>
                )}

                <button
                    type="submit"
                    className="login-btn"
                    data-test="login-submit"
                    disabled={loading}
                >
                    {loading ? 'Accesso in corso...' : 'Entra'}
                </button>
            </form>
        </div>
    );
}
