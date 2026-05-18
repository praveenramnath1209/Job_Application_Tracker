import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { X, Loader2, Pencil } from 'lucide-react';
import toast from 'react-hot-toast';
import { applicationsApi } from '../../api/applications';
import { getErrorMessage } from '../../utils/formatters';

const schema = z.object({
  companyName: z.string().min(1, 'Required').max(150),
  role: z.string().min(1, 'Required').max(150),
  jobUrl: z.string().url('Must be a valid URL').or(z.literal('')).optional(),
  ctcPackage: z.string().optional(),
  dateApplied: z.string().optional(),
  notes: z.string().max(5000).optional(),
});

export default function EditApplicationModal({ application, onClose, onSuccess }) {
  const [loading, setLoading] = useState(false);

  const { register, handleSubmit, formState: { errors } } = useForm({
    resolver: zodResolver(schema),
    defaultValues: {
      companyName: application.companyName || '',
      role:        application.role || '',
      jobUrl:      application.jobUrl || '',
      ctcPackage:  application.ctcPackage != null ? String(application.ctcPackage) : '',
      dateApplied: application.dateApplied || '',
      notes:       application.notes || '',
    },
  });

  const onSubmit = async (data) => {
    setLoading(true);
    try {
      const payload = {
        companyName: data.companyName,
        role:        data.role,
        jobUrl:      data.jobUrl || null,
        ctcPackage:  data.ctcPackage ? parseFloat(data.ctcPackage) : null,
        dateApplied: data.dateApplied || null,
        notes:       data.notes || null,
        status:      application.status,   // preserve existing status
      };
      const res = await applicationsApi.update(application.id, payload);
      toast.success('Application updated!');
      onSuccess(res.data.data);
    } catch (err) {
      toast.error(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="modal-content">
        {/* Header */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Pencil size={18} color="var(--brand-color)" />
            <h2 style={{ fontSize: '1.1rem', fontWeight: 700, color: 'var(--text-primary)' }}>
              Edit Application
            </h2>
          </div>
          <button onClick={onClose} className="btn-icon">
            <X size={20} />
          </button>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div>
              <label className="label">Company Name *</label>
              <input {...register('companyName')} className="input-field" placeholder="Google" />
              {errors.companyName && <p className="error-text">{errors.companyName.message}</p>}
            </div>
            <div>
              <label className="label">Role *</label>
              <input {...register('role')} className="input-field" placeholder="Software Engineer" />
              {errors.role && <p className="error-text">{errors.role.message}</p>}
            </div>
          </div>

          <div>
            <label className="label">Job URL</label>
            <input {...register('jobUrl')} className="input-field" placeholder="https://..." type="url" />
            {errors.jobUrl && <p className="error-text">{errors.jobUrl.message}</p>}
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div>
              <label className="label">CTC Package (LPA)</label>
              <input {...register('ctcPackage')} className="input-field" placeholder="12.5" type="number" step="0.1" min="0" />
            </div>
            <div>
              <label className="label">Date Applied</label>
              <input {...register('dateApplied')} className="input-field" type="date" />
            </div>
          </div>

          <div>
            <label className="label">Notes</label>
            <textarea
              {...register('notes')}
              className="input-field"
              placeholder="Referral, recruiter contact, interview notes..."
              rows={3}
              style={{ resize: 'vertical', fontFamily: 'inherit' }}
            />
          </div>

          <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'flex-end', marginTop: '0.5rem' }}>
            <button type="button" onClick={onClose} className="btn-secondary">Cancel</button>
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? <Loader2 size={16} /> : <Pencil size={16} />}
              {loading ? 'Saving...' : 'Save Changes'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
