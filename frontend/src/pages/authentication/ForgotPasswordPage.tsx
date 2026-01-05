// src/pages/password/ForgotPasswordPage.tsx (adatta il path se diverso)

import { useState } from 'react';
import { Link } from 'react-router-dom';
import { forgotPassword } from '../../services/passwordService';

import '../../styles/password.css';

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
                // TC3_01 / TC3_03 – cliente esistente → defaultPassword = "Cliente123!"
                setSuccessMsg(
                    `Abbiamo impostato una password di default: ${res.defaultPassword}. ` +
                    'Usala per accedere e ti verrà chiesto di scegliere una nuova password.',
                );
            } else {
                // TC3_02 – email inesistente / nulla / vuota → success=false
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

                {error && (
                    <div
                        className="pw-alert pw-alert--error"
                        data-test="forgot-error"
                    >
                        {error}
                    </div>
                )}
                {successMsg && (
                    <div
                        className="pw-alert pw-alert--success"
                        data-test="forgot-success"
                    >
                        {successMsg}
                    </div>
                )}

                <form
                    className="pw-form"
                    onSubmit={handleSubmit}
                    data-test="forgot-form"
                >
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
                            data-test="forgot-email"
                        />
                    </div>

                    <button
                        type="submit"
                        disabled={loading}
                        className="pw-btn"
                        data-test="forgot-submit"
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
