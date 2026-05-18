import React from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';

export default function AppLayout() {
  return (
    <div style={{ display: 'flex', minHeight: '100vh', background: 'var(--bg-primary)', transition: 'background 0.25s ease' }}>
      <Sidebar />
      <main style={{ flex: 1, overflow: 'auto', padding: '2rem', minWidth: 0, background: 'var(--bg-primary)' }}>
        <div style={{ maxWidth: '1400px', margin: '0 auto' }}>
          <Outlet />
        </div>
      </main>
    </div>
  );
}
