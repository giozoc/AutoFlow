// src/pages/auth/LoginPage.tsx

import { useState } from 'react';
import {Link, useNavigate} from 'react-router-dom';
import { login } from '../../services/authService';

import './login.css';

export default function LoginPage() {
    const navigate = useNavigate();
    const [email, setEmail] = useState('admin@autoflow.it');
    const [password, setPassword] = useState('Admin123!');
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();
        setLoading(true);
        setError(null);

        try {
            const auth = await login({ email, password });

            if (auth.mustChangePassword) {
                // vai alla pagina di cambio password obbligatoria
                navigate('/password/first-change', {
                    replace: true,
                    state: { email },  // per mostrare l'email disabled
                });
                return;
            }

            if (auth.ruolo === 'AMMINISTRATORE' || auth.ruolo === 'ADDETTO_VENDITE') {
                navigate('/admin', { replace: true });
            } else {
                navigate('/showroom', { replace: true });
            }
        } catch (err) {
            console.error(err);
            setError('Credenziali non valide o errore di connessione.');
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="login-page">
            <form className="login-card" onSubmit={handleSubmit}>
                <h1 className="login-title">AutoFlow – Login</h1>

                <div className="login-group">
                    <label className="login-label">Email</label>
                    <input
                        type="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        className="login-input"
                        required
                    />
                </div>

                <div className="login-group">
                    <label className="login-label">Password</label>
                    <input
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        className="login-input"
                        required
                    />
                </div>
                <p className="login-forgot">
                    Hai dimenticato la password?{' '}
                    <Link to="/password/forgot">Recupera password</Link>
                </p>

                {error && <div className="login-error">{error}</div>}

                <button type="submit" className="login-btn" disabled={loading}>
                    {loading ? 'Accesso in corso...' : 'Entra'}
                </button>
            </form>
        </div>
    );
}