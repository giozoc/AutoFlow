import { useState } from 'react';
import { Link } from 'react-router-dom';
import { forgotPassword } from '../../services/passwordService';

import './password.css';

const ForgotPasswordPage: React.FC = () => {
    const [email, setEmail] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [successMsg, setSuccessMsg] = useState<string | null>(null);

    async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();
        setLoading(true);
        setError(null);
        setSuccessMsg(null);

        try {
            const res = await forgotPassword({ email });

            if (res.success && res.defaultPassword) {
                setSuccessMsg(
                    `Abbiamo impostato una password di default: ${res.defaultPassword}. ` +
                    'Usala per accedere e ti verrà chiesto di scegliere una nuova password.',
                );
            } else {
                // email non esistente o altro → messaggio generico
                setSuccessMsg(
                    'Se l’email è presente nei nostri sistemi, è stata impostata una password temporanea.',
                );
            }
        } catch (err) {
            console.error(err);
            setError('Errore durante la richiesta di recupero password.');
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="pw-page">
            <div className="pw-card">
                <h1 className="pw-title">Recupero password</h1>
                <p className="pw-subtitle">
                    Inserisci la tua email. Se sei registrato come{' '}
                    <strong>CLIENTE</strong> o <strong>ADDETTO_VENDITE</strong>,
                    imposteremo una password temporanea.
                </p>

                {error && <div className="pw-alert pw-alert--error">{error}</div>}
                {successMsg && (
                    <div className="pw-alert pw-alert--success">{successMsg}</div>
                )}

                <form className="pw-form" onSubmit={handleSubmit}>
                    <div className="pw-group">
                        <label className="pw-label" htmlFor="email">
                            Email
                        </label>
                        <input
                            id="email"
                            type="email"
                            className="pw-input"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                        />
                    </div>

                    <button
                        type="submit"
                        disabled={loading}
                        className="pw-btn"
                    >
                        {loading ? 'Invio in corso...' : 'Imposta password di default'}
                    </button>
                </form>

                <p className="pw-footer">
                    Ricordi la password?{' '}
                    <Link to="/login" className="pw-link">
                        Torna al login
                    </Link>
                </p>
            </div>
        </div>
    );
};

export default ForgotPasswordPage;
