// src/pages/password/FirstPasswordChangePage.tsx (adatta il path se diverso)

import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { changePasswordAfterReset } from '../../services/passwordService';
import { logout } from '../../services/authService';

import './password.css';

interface LocationState {
    email?: string;
}

const FirstPasswordChangePage: React.FC = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const state = (location.state as LocationState) || {};
    const email = state.email ?? '';

    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState<string | null>(null);

    async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();
        setError(null);
        setSuccess(null);

        if (newPassword !== confirmPassword) {
            // Caso FE specifico per TC3: password non coincidono
            setError('Le password non coincidono.');
            return;
        }

        setLoading(true);
        try {
            const ok = await changePasswordAfterReset(newPassword);
            if (!ok) {
                // token inesistente o utente già attivo → TC3_05 / TC3_06 (dal punto di vista FE sono uguali)
                setError(
                    'Impossibile aggiornare la password. Riprova il recupero password.',
                );
                return;
            }

            setSuccess(
                'Password aggiornata con successo. Verrai reindirizzato alla schermata di login.',
            );

            // facciamo logout e torniamo al login
            await logout();
            setTimeout(() => navigate('/login', { replace: true }), 1500);
        } catch (err) {
            console.error(err);
            setError('Errore durante il cambio password.');
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="pw-page">
            <div className="pw-card">
                <h1 className="pw-title">Imposta una nuova password</h1>
                <p className="pw-subtitle">
                    Per continuare a utilizzare AutoFlow devi scegliere una
                    password personale.
                </p>

                {error && (
                    <div
                        className="pw-alert pw-alert--error"
                        data-test="first-change-error"
                    >
                        {error}
                    </div>
                )}
                {success && (
                    <div
                        className="pw-alert pw-alert--success"
                        data-test="first-change-success"
                    >
                        {success}
                    </div>
                )}

                <form
                    className="pw-form"
                    onSubmit={handleSubmit}
                    data-test="first-change-form"
                >
                    <div className="pw-group">
                        <label className="pw-label">Email</label>
                        <input
                            className="pw-input"
                            value={email}
                            disabled
                            data-test="first-change-email"
                        />
                    </div>

                    <div className="pw-group">
                        <label className="pw-label" htmlFor="newPassword">
                            Nuova password
                        </label>
                        <input
                            id="newPassword"
                            type="password"
                            className="pw-input"
                            value={newPassword}
                            onChange={(e) => setNewPassword(e.target.value)}
                            required
                            data-test="first-change-new"
                        />
                    </div>

                    <div className="pw-group">
                        <label className="pw-label" htmlFor="confirmPassword">
                            Conferma nuova password
                        </label>
                        <input
                            id="confirmPassword"
                            type="password"
                            className="pw-input"
                            value={confirmPassword}
                            onChange={(e) =>
                                setConfirmPassword(e.target.value)
                            }
                            required
                            data-test="first-change-confirm"
                        />
                    </div>

                    <button
                        type="submit"
                        className="pw-btn"
                        disabled={loading}
                        data-test="first-change-submit"
                    >
                        {loading ? 'Salvataggio...' : 'Cambia password'}
                    </button>
                </form>
            </div>
        </div>
    );
};

export default FirstPasswordChangePage;
