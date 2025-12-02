import { Routes, Route, Navigate } from 'react-router-dom'
import LoginPage from '../pages/authentication/LoginPage'
import AdminDashboard from '../pages/user/AdminDashboard'
import { ProtectedRoute } from '../components/layout/ProtectedRoute'
import StaffListPage from '../pages/user/StaffListPage'
import ClientListPage from "../pages/user/ClientListPage.tsx"
import VehicleListPage from '../pages/vehicle/VehicleListPage'
import ShowroomPage from '../pages/showroom/ShowroomPage'
import ShowroomDettaglioPage from '../pages/showroom/ShowroomDettaglioPage'
import OptionalListPage from '../pages/configuration/OptionalListPage'
import ConfigurationListPage from '../pages/configuration/ConfigurationListPage'
import RegisterClientePage from '../pages/authentication/RegisterClientePage'
import ClientConfigurationPage from '../pages/configuration/ClientConfigurationPage'
import ClientProposalsListPage from '../pages/proposal/ClientProposalsListPage';
import ClientPropostaDetailPage from '../pages/proposal/ClientPropostaDetailPage';
import AdminProposalsListPage from '../pages/proposal/AdminProposalsListPage';
import AdminPropostaEditPage from '../pages/proposal/AdminPropostaEditPage';
import AdminFattureListPage from '../pages/invoice/AdminFattureListPage'
import ClientFattureListPage from '../pages/invoice/ClientFattureListPage'
import ConfigurazioneRiepilogoPage from '../pages/configuration/ConfigurazioneRiepilogoPage'
import StatisticsPage from "../pages/statistics/StatisticsPage.tsx";
import ForgotPasswordPage from "../pages/authentication/ForgotPasswordPage.tsx";
import FirstPasswordChangePage from "../pages/authentication/FirstPasswordChangePage.tsx";
import ProfilePage from "../pages/user/ProfilePage.tsx";

const AppRoutes: React.FC = () => {
    return (
        <Routes>
            <Route path="/" element={<ShowroomPage />} />
            <Route path="/showroom" element={<ShowroomPage />} />
            <Route path="/showroom/:id" element={<ShowroomDettaglioPage />} />
            <Route path="/register" element={<RegisterClientePage />} />

            <Route path="/login" element={<LoginPage/>}/>

            <Route path="/password/forgot" element={<ForgotPasswordPage />} />
            <Route path="/password/first-change" element={<FirstPasswordChangePage />} />
            <Route path="/profilo" element={<ProfilePage />} />

            <Route
                path="/cliente/proposte"
                element={
                    <ProtectedRoute allowedRoles={['CLIENTE']}>
                        <ClientProposalsListPage />
                    </ProtectedRoute>
                }
            />
            <Route
                path="/cliente/proposte/:id"
                element={
                    <ProtectedRoute allowedRoles={['CLIENTE']}>
                        <ClientPropostaDetailPage />
                    </ProtectedRoute>
                }
            />

            <Route
                path="/configurazioni/mie"
                element={
                    <ProtectedRoute allowedRoles={['CLIENTE']}>
                        <ClientConfigurationPage />
                    </ProtectedRoute>
                }
            />

            <Route
                path="/configurazioni/:id/riepilogo"
                element={
                    <ProtectedRoute allowedRoles={['CLIENTE']}>
                        <ConfigurazioneRiepilogoPage />
                    </ProtectedRoute>
                }
            />



            <Route
                path="/admin"
                element={
                    <ProtectedRoute allowedRoles={['AMMINISTRATORE', 'ADDETTO_VENDITE']}>
                        <AdminDashboard />
                    </ProtectedRoute>
                }
            />

            <Route
                path="/admin/staff"
                element={
                    <ProtectedRoute allowedRoles={['AMMINISTRATORE']}>
                        <StaffListPage/>
                    </ProtectedRoute>
                }
            />

            <Route
                path="/admin/clienti"
                element={
                    <ProtectedRoute allowedRoles={['AMMINISTRATORE', 'ADDETTO_VENDITE']}>
                        <ClientListPage/>
                    </ProtectedRoute>
                }
            />

            <Route
                path="/admin/veicoli"
                element={
                    <ProtectedRoute allowedRoles={['AMMINISTRATORE', 'ADDETTO_VENDITE']}>
                        <VehicleListPage />
                    </ProtectedRoute>
                }
            />

            <Route
                path="/admin/optional"
                element={
                    <ProtectedRoute allowedRoles={['AMMINISTRATORE', 'ADDETTO_VENDITE']}>
                        <OptionalListPage />
                    </ProtectedRoute>
                }
            />

            <Route
                path="/admin/configurazioni"
                element={
                    <ProtectedRoute allowedRoles={['AMMINISTRATORE', 'ADDETTO_VENDITE']}>
                        <ConfigurationListPage />
                    </ProtectedRoute>
                }
            />

            <Route
                path="/admin/configurazioni/:id/riepilogo"
                element={
                    <ProtectedRoute allowedRoles={['AMMINISTRATORE', 'ADDETTO_VENDITE']}>
                        <ConfigurazioneRiepilogoPage />
                    </ProtectedRoute>
                }
            />

            <Route
                path="/admin/proposte"
                element={
                    <ProtectedRoute allowedRoles={['AMMINISTRATORE', 'ADDETTO_VENDITE']}>
                        <AdminProposalsListPage />
                    </ProtectedRoute>
                }
            />
            <Route
                path="/admin/proposte/:id"
                element={
                    <ProtectedRoute allowedRoles={['AMMINISTRATORE', 'ADDETTO_VENDITE']}>
                        <AdminPropostaEditPage />
                    </ProtectedRoute>
                }
            />

            {/* FATTURE GESTIONALE */}
            <Route
                path="/admin/fatture"
                element={
                    <ProtectedRoute allowedRoles={['AMMINISTRATORE', 'ADDETTO_VENDITE']}>
                        <AdminFattureListPage />
                    </ProtectedRoute>
                }
            />

            <Route
                path="/admin/statistics"
                element={
                    <ProtectedRoute allowedRoles={['AMMINISTRATORE', 'ADDETTO_VENDITE']}>
                        <StatisticsPage />
                    </ProtectedRoute>
                }
            />

            {/* FATTURE CLIENTE */}
            <Route
                path="/cliente/fatture"
                element={
                    <ProtectedRoute allowedRoles={['CLIENTE']}>
                        <ClientFattureListPage />
                    </ProtectedRoute>
                }
            />

            {/* se vai su / ti porto alla dashboard (se non sei loggato, ProtectedRoute ti manda a /login) */}
            <Route path="/" element={<Navigate to="/admin" replace/>}/>

            {/* qualunque altra rotta sconosciuta → /login */}
            <Route path="*" element={<Navigate to="/login" replace/>}/>
        </Routes>
    );
}

export default AppRoutes