import React, { useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import AppLayout from './components/layout/AppLayout';
import ProtectedRoute from './components/layout/ProtectedRoute';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import ApplicationsPage from './pages/ApplicationsPage';
import AnalyticsPage from './pages/AnalyticsPage';
import AnalyzerPage from './pages/AnalyzerPage';
import { useThemeStore } from './store/themeStore';

export default function App() {
  const theme = useThemeStore((state) => state.theme);

  useEffect(() => {
    // Apply current theme to document
    document.documentElement.setAttribute('data-theme', theme);
  }, [theme]);

  useEffect(() => {
    // One-time migration: if user has never chosen a theme manually, reset to light
    const stored = localStorage.getItem('theme-storage');
    if (stored) {
      try {
        const parsed = JSON.parse(stored);
        if (!parsed?.state?.theme) {
          localStorage.removeItem('theme-storage');
        }
      } catch { localStorage.removeItem('theme-storage'); }
    }
  }, []);

  return (
    <BrowserRouter>
      <Toaster
        position="top-right"
        toastOptions={{
          className: 'custom-toast',
          style: {
            background: 'var(--bg-card)',
            color: 'var(--text-primary)',
            border: '1px solid var(--border-color)',
            borderRadius: '10px',
            fontSize: '0.875rem',
          },
          success: { iconTheme: { primary: 'var(--success-color)', secondary: 'var(--bg-card)' } },
          error: { iconTheme: { primary: 'var(--danger-color)', secondary: 'var(--bg-card)' } },
        }}
      />
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <AppLayout />
            </ProtectedRoute>
          }
        >
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<DashboardPage />} />
          <Route path="applications" element={<ApplicationsPage />} />
          <Route path="analytics" element={<AnalyticsPage />} />
          <Route path="analyzer" element={<AnalyzerPage />} />
        </Route>
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
