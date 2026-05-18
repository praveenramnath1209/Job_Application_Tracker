import React, { useEffect, useState, useCallback } from 'react';
import { Plus, Search, ExternalLink, Trash2, Pencil, Check, X } from 'lucide-react';
import toast from 'react-hot-toast';
import { applicationsApi } from '../api/applications';
import { STANDARD_STATUSES } from '../utils/constants';
import { formatDate, formatCTC, getErrorMessage } from '../utils/formatters';
import AddApplicationModal from '../components/applications/AddApplicationModal';
import EditApplicationModal from '../components/applications/EditApplicationModal';

/** Converts a status string to the matching CSS class name, e.g. "Moved to Next Round" → "standard-moved-to-next-round" */
function statusClass(status) {
  if (!STANDARD_STATUSES.includes(status)) return 'custom-status';
  return 'standard-' + status.toLowerCase().replace(/\s+/g, '-');
}

function StatusDropdown({ application, onStatusUpdate }) {
  const [isEditingCustom, setIsEditingCustom] = useState(false);
  const [customValue, setCustomValue] = useState('');
  const [loading, setLoading] = useState(false);

  const currentStatus = application.status || 'Applied';
  const isStandard = STANDARD_STATUSES.includes(currentStatus);

  const handleSelectChange = async (e) => {
    const value = e.target.value;
    if (value === '__custom__') {
      setIsEditingCustom(true);
      setCustomValue('');
    } else {
      await updateStatus(value);
    }
  };

  const handleCustomSubmit = async () => {
    if (!customValue.trim()) { setIsEditingCustom(false); return; }
    await updateStatus(customValue.trim());
    setIsEditingCustom(false);
  };

  const updateStatus = async (newStatus) => {
    if (newStatus === currentStatus) return;
    setLoading(true);
    try {
      await applicationsApi.updateStatus(application.id, newStatus);
      onStatusUpdate(application.id, newStatus);
      toast.success('Status updated');
    } catch (err) {
      toast.error(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  if (isEditingCustom) {
    return (
      <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
        <input
          type="text"
          autoFocus
          className="input-field"
          style={{ padding: '4px 8px', fontSize: '0.8rem', width: '130px' }}
          placeholder="e.g. Round 2"
          value={customValue}
          onChange={(e) => setCustomValue(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleCustomSubmit()}
        />
        <button onClick={handleCustomSubmit} className="btn-icon" style={{ color: 'var(--success-color)' }}>
          <Check size={14} />
        </button>
        <button onClick={() => setIsEditingCustom(false)} className="btn-icon" style={{ color: 'var(--danger-color)' }}>
          <X size={14} />
        </button>
      </div>
    );
  }

  return (
    <select
      value={isStandard ? currentStatus : '__current__'}
      onChange={handleSelectChange}
      disabled={loading}
      className={`status-select ${statusClass(currentStatus)}`}
      style={{
        padding: '4px 8px',
        borderRadius: '6px',
        fontSize: '0.8rem',
        fontWeight: 600,
        border: '1px solid',
        cursor: 'pointer',
        maxWidth: '180px',
      }}
    >
      {STANDARD_STATUSES.map((s) => (
        <option key={s} value={s}>{s}</option>
      ))}
      {/* Show the current custom status as an option so it appears selected */}
      {!isStandard && (
        <option value="__current__">{currentStatus}</option>
      )}
      <option value="__custom__">✏️ Custom Status...</option>
    </select>
  );
}

export default function ApplicationsPage() {
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showAddModal, setShowAddModal] = useState(false);
  const [editingApp, setEditingApp] = useState(null);   // application to edit
  const [search, setSearch] = useState('');

  const fetchApplications = useCallback(async () => {
    try {
      const res = await applicationsApi.getAll();
      setApplications(res.data.data || []);
    } catch (err) {
      toast.error(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchApplications(); }, [fetchApplications]);

  const handleDelete = async (id, companyName) => {
    if (!window.confirm(`Delete application for ${companyName}?`)) return;
    try {
      await applicationsApi.delete(id);
      setApplications((prev) => prev.filter((a) => a.id !== id));
      toast.success('Application deleted');
    } catch (err) {
      toast.error(getErrorMessage(err));
    }
  };

  const handleStatusUpdate = (id, newStatus) => {
    setApplications((prev) => prev.map((a) => (a.id === id ? { ...a, status: newStatus } : a)));
  };

  const handleEditSuccess = (updatedApp) => {
    setApplications((prev) => prev.map((a) => (a.id === updatedApp.id ? updatedApp : a)));
    setEditingApp(null);
  };

  const filteredApps = applications.filter(
    (a) =>
      a.companyName.toLowerCase().includes(search.toLowerCase()) ||
      a.role.toLowerCase().includes(search.toLowerCase()),
  );

  if (loading) {
    return (
      <div className="fade-in">
        <div className="skeleton" style={{ height: '40px', width: '200px', marginBottom: '1rem' }} />
        <div className="skeleton" style={{ height: '400px', width: '100%', borderRadius: '12px' }} />
      </div>
    );
  }

  return (
    <div className="fade-in">
      {/* Header */}
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 className="page-title">Applications</h1>
          <p className="page-subtitle">Track and manage your job applications</p>
        </div>
        <button className="btn-primary" onClick={() => setShowAddModal(true)}>
          <Plus size={16} /> New Application
        </button>
      </div>

      {/* Table card */}
      <div className="card" style={{ padding: '0', overflow: 'hidden' }}>
        {/* Search bar */}
        <div style={{
          padding: '0.875rem 1rem',
          borderBottom: '1px solid var(--border-color)',
          display: 'flex', alignItems: 'center', gap: '0.5rem',
          background: 'var(--bg-secondary)',
        }}>
          <Search size={16} color="var(--text-muted)" />
          <input
            type="text"
            placeholder="Search company or role..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            style={{
              border: 'none', background: 'transparent',
              color: 'var(--text-primary)', outline: 'none', flex: 1,
              fontSize: '0.875rem',
            }}
          />
          {filteredApps.length > 0 && (
            <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', whiteSpace: 'nowrap' }}>
              {filteredApps.length} result{filteredApps.length !== 1 ? 's' : ''}
            </span>
          )}
        </div>

        {/* Table */}
        <div style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid var(--border-color)', background: 'var(--bg-secondary)' }}>
                {['Company', 'Role', 'Status', 'Date Applied', 'Actions'].map((h) => (
                  <th key={h} style={{
                    padding: '0.75rem 1rem',
                    fontSize: '0.75rem', fontWeight: 700,
                    color: 'var(--text-muted)',
                    textTransform: 'uppercase', letterSpacing: '0.05em',
                  }}>
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {filteredApps.length === 0 ? (
                <tr>
                  <td colSpan={5} style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                    {search ? 'No applications match your search.' : 'No applications yet. Add your first one!'}
                  </td>
                </tr>
              ) : (
                filteredApps.map((app) => (
                  <tr
                    key={app.id}
                    style={{ borderBottom: '1px solid var(--border-color)', transition: 'background 0.15s' }}
                    onMouseEnter={(e) => (e.currentTarget.style.background = 'var(--bg-secondary)')}
                    onMouseLeave={(e) => (e.currentTarget.style.background = 'transparent')}
                  >
                    {/* Company */}
                    <td style={{ padding: '0.875rem 1rem', fontWeight: 600, color: 'var(--text-primary)' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                        {app.companyName}
                        {app.jobUrl && (
                          <a
                            href={app.jobUrl} target="_blank" rel="noopener noreferrer"
                            style={{ color: 'var(--brand-color)', display: 'flex', alignItems: 'center' }}
                          >
                            <ExternalLink size={13} />
                          </a>
                        )}
                      </div>
                    </td>

                    {/* Role + CTC */}
                    <td style={{ padding: '0.875rem 1rem', color: 'var(--text-secondary)' }}>
                      {app.role}
                      {app.ctcPackage && (
                        <div style={{ fontSize: '0.75rem', color: 'var(--success-color)', marginTop: '2px' }}>
                          {formatCTC(app.ctcPackage)}
                        </div>
                      )}
                    </td>

                    {/* Status dropdown */}
                    <td style={{ padding: '0.875rem 1rem' }}>
                      <StatusDropdown application={app} onStatusUpdate={handleStatusUpdate} />
                    </td>

                    {/* Date */}
                    <td style={{ padding: '0.875rem 1rem', color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
                      {app.dateApplied ? formatDate(app.dateApplied) : '—'}
                    </td>

                    {/* Actions */}
                    <td style={{ padding: '0.875rem 1rem' }}>
                      <div style={{ display: 'flex', gap: '4px' }}>
                        <button
                          onClick={() => setEditingApp(app)}
                          className="btn-icon"
                          title="Edit application"
                          style={{ color: 'var(--brand-color)' }}
                        >
                          <Pencil size={15} />
                        </button>
                        <button
                          onClick={() => handleDelete(app.id, app.companyName)}
                          className="btn-icon"
                          title="Delete application"
                          style={{ color: 'var(--danger-color)' }}
                        >
                          <Trash2 size={15} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Add modal */}
      {showAddModal && (
        <AddApplicationModal
          onClose={() => setShowAddModal(false)}
          onSuccess={(newApp) => {
            setApplications((prev) => [newApp, ...prev]);
            setShowAddModal(false);
          }}
        />
      )}

      {/* Edit modal */}
      {editingApp && (
        <EditApplicationModal
          application={editingApp}
          onClose={() => setEditingApp(null)}
          onSuccess={handleEditSuccess}
        />
      )}
    </div>
  );
}
