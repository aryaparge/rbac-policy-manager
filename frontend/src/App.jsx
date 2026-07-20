import { Routes, Route, Navigate, Link } from 'react-router-dom';
import { AuthProvider, useAuth } from './auth/AuthContext';
import ProtectedRoute from './auth/ProtectedRoute';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import EntityListPage from './pages/EntityListPage';
import RoleDetailPage from './pages/RoleDetailPage';
import GroupDetailPage from './pages/GroupDetailPage';
import SubjectDetailPage from './pages/SubjectDetailPage';
import AuthorizationCheckPage from './pages/AuthorizationCheckPage';

function Shell({ children }) {
  const { username, logout } = useAuth();
  return (
    <div className="app-shell">
      <header className="app-header">
        <Link to="/" className="app-title">RBAC Policy Manager</Link>
        {username && (
          <div className="header-right">
            <span className="muted">{username}</span>
            <button className="link-button" onClick={logout}>Log out</button>
          </div>
        )}
      </header>
      <main className="app-main">{children}</main>
    </div>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route
          path="/*"
          element={
            <ProtectedRoute>
              <Shell>
                <Routes>
                  <Route path="/" element={<DashboardPage />} />
                  <Route path="/authorization-check" element={<AuthorizationCheckPage />} />
                  <Route path="/entities/:entityType" element={<EntityListPage />} />
                  <Route path="/roles/:id" element={<RoleDetailPage />} />
                  <Route path="/groups/:id" element={<GroupDetailPage />} />
                  <Route path="/subjects/:id" element={<SubjectDetailPage />} />
                  <Route path="*" element={<Navigate to="/" replace />} />
                </Routes>
              </Shell>
            </ProtectedRoute>
          }
        />
      </Routes>
    </AuthProvider>
  );
}
