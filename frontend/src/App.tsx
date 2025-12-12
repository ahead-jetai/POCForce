import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import { Login } from './pages/Login';
import { Register } from './pages/Register';
import { Dashboard } from './pages/Dashboard';
import { PocList } from './pages/PocList';
import { PocDetail } from './pages/PocDetail';
import { PocForm } from './pages/PocForm';
import { Layout } from './components/Layout';

const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" />;
};

const PublicRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useAuth();
  return !isAuthenticated ? <>{children}</> : <Navigate to="/" />;
};

const AppRoutes: React.FC = () => {
  return (
    <Routes>
      <Route
        path="/login"
        element={
          <PublicRoute>
            <Login />
          </PublicRoute>
        }
      />
      <Route
        path="/register"
        element={
          <PublicRoute>
            <Register />
          </PublicRoute>
        }
      />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <Layout>
              <Dashboard />
            </Layout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/pocs"
        element={
          <ProtectedRoute>
            <Layout>
              <PocList />
            </Layout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/pocs/new"
        element={
          <ProtectedRoute>
            <Layout>
              <PocForm />
            </Layout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/pocs/:id"
        element={
          <ProtectedRoute>
            <Layout>
              <PocDetail />
            </Layout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/pocs/:id/edit"
        element={
          <ProtectedRoute>
            <Layout>
              <PocForm />
            </Layout>
          </ProtectedRoute>
        }
      />
      <Route path="*" element={<Navigate to="/" />} />
    </Routes>
  );
};

const App: React.FC = () => {
  return (
    <Router>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </Router>
  );
};

export default App;
