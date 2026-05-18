import React, { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import {
  LayoutDashboard, BarChart3, FileSearch,
  LogOut, ChevronLeft, ChevronRight, Briefcase, Sun, Moon, ListChecks
} from 'lucide-react';
import { useAuthStore } from '../../store/authStore';
import { useThemeStore } from '../../store/themeStore';

const NAV_ITEMS = [
  { to: '/dashboard',    icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/applications', icon: ListChecks,      label: 'Applications' },
  { to: '/analytics',   icon: BarChart3,       label: 'Analytics' },
  { to: '/analyzer',    icon: FileSearch,      label: 'Resume Analyzer' },
];

export default function Sidebar() {
  const [collapsed, setCollapsed] = useState(false);
  const { user, logout } = useAuthStore();
  const { theme, toggleTheme } = useThemeStore();
  const navigate = useNavigate();

  const handleLogout = () => { logout(); navigate('/login'); };

  return (
    <aside
      style={{
        width: collapsed ? '68px' : '240px',
        minHeight: '100vh',
        background: 'var(--sidebar-bg)',
        borderRight: '1px solid var(--sidebar-border)',
        display: 'flex',
        flexDirection: 'column',
        transition: 'width 0.3s ease, background 0.25s ease',
        position: 'relative',
        flexShrink: 0,
        boxShadow: '1px 0 0 var(--border-color)',
      }}
    >
      {/* Logo */}
      <div style={{
        padding: collapsed ? '1.25rem 0' : '1.25rem 1rem',
        borderBottom: '1px solid var(--sidebar-border)',
        display: 'flex',
        alignItems: 'center',
        gap: '0.75rem',
        justifyContent: collapsed ? 'center' : 'flex-start',
      }}>
        <div style={{
          width: '36px', height: '36px',
          background: 'linear-gradient(135deg, var(--brand-color), #8b5cf6)',
          borderRadius: '10px',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          flexShrink: 0,
          boxShadow: '0 2px 8px var(--brand-glow)',
        }}>
          <Briefcase size={18} color="white" />
        </div>
        {!collapsed && (
          <div>
            <div style={{ fontWeight: 700, fontSize: '0.95rem', color: 'var(--text-primary)' }}>JobTracker</div>
            <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>Pro Dashboard</div>
          </div>
        )}
      </div>

      {/* Nav */}
      <nav style={{ flex: 1, padding: '0.75rem 0.5rem', display: 'flex', flexDirection: 'column', gap: '2px' }}>
        {NAV_ITEMS.map(({ to, icon: Icon, label }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) => `sidebar-link${isActive ? ' active' : ''}`}
            title={collapsed ? label : undefined}
            style={{ justifyContent: collapsed ? 'center' : 'flex-start' }}
          >
            <Icon size={18} style={{ flexShrink: 0 }} />
            {!collapsed && <span>{label}</span>}
          </NavLink>
        ))}
      </nav>

      {/* Bottom section */}
      <div style={{ padding: '0.75rem 0.5rem', borderTop: '1px solid var(--sidebar-border)' }}>
        {!collapsed && user && (
          <div style={{
            padding: '0.5rem 0.75rem', marginBottom: '0.5rem',
            background: 'var(--brand-glow)', borderRadius: '0.5rem',
            border: '1px solid rgba(79,70,229,0.15)',
          }}>
            <div style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-primary)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
              {user.username}
            </div>
            <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
              {user.email}
            </div>
          </div>
        )}

        {/* Theme toggle */}
        <button
          onClick={toggleTheme}
          className="sidebar-link"
          title={collapsed ? (theme === 'dark' ? 'Light Mode' : 'Dark Mode') : undefined}
          style={{ justifyContent: collapsed ? 'center' : 'flex-start', marginBottom: '0.25rem' }}
        >
          {theme === 'dark'
            ? <Sun size={18} style={{ flexShrink: 0, color: 'var(--warning-color)' }} />
            : <Moon size={18} style={{ flexShrink: 0 }} />}
          {!collapsed && <span>{theme === 'dark' ? 'Light Mode' : 'Dark Mode'}</span>}
        </button>

        {/* Logout */}
        <button
          onClick={handleLogout}
          className="sidebar-link"
          title={collapsed ? 'Logout' : undefined}
          style={{ justifyContent: collapsed ? 'center' : 'flex-start', color: 'var(--danger-color)' }}
        >
          <LogOut size={18} style={{ flexShrink: 0 }} />
          {!collapsed && <span>Logout</span>}
        </button>
      </div>

      {/* Collapse toggle */}
      <button
        onClick={() => setCollapsed(!collapsed)}
        style={{
          position: 'absolute', top: '1.25rem', right: '-13px',
          width: '26px', height: '26px',
          background: 'var(--sidebar-bg)',
          border: '1px solid var(--sidebar-border)',
          borderRadius: '50%',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          cursor: 'pointer', color: 'var(--text-muted)',
          transition: 'all 0.2s',
          zIndex: 10,
          boxShadow: '0 1px 4px rgba(0,0,0,0.08)',
        }}
      >
        {collapsed ? <ChevronRight size={14} /> : <ChevronLeft size={14} />}
      </button>
    </aside>
  );
}
