import { Navigate } from 'react-router-dom';
import { getAuthState } from '../../services/authService';
import type { Ruolo } from '../../entities/auth';

import type { JSX } from "react";

interface ProtectedRouteProps {
    children: JSX.Element;
    allowedRoles?: Ruolo[];
}

/**
 * Se l'utente non è loggato → /login
 * Se il ruolo non è permesso → /login (per ora)
 */
export function ProtectedRoute({ children, allowedRoles }: ProtectedRouteProps) {
    const { token, ruolo } = getAuthState();

    if (!token || !ruolo) {
        return <Navigate to="/login" replace />;
    }

    if (allowedRoles && !allowedRoles.includes(ruolo)) {
        return <Navigate to="/login" replace />;
    }

    return children;
}