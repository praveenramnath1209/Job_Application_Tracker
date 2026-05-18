import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Briefcase, TrendingUp, Award, Calendar, ArrowRight } from 'lucide-react';
import { analyticsApi } from '../api/analytics';
import { useAuthStore } from '../store/authStore';

function StatCard({ icon: Icon, label, value, color, subtitle }) {
  return (
    <div className="stat-card">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.8rem', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em' }}>{label}</p>
          <p style={{ fontSize: '2rem', fontWeight: 800, color: 'var(--text-primary)', marginTop: '0.25rem' }}>{value}</p>
          {subtitle && <p style={{ color: 'var(--text-muted)', fontSize: '0.75rem', marginTop: '0.25rem' }}>{subtitle}</p>}
        </div>
        <div style={{
          width: '44px', height: '44px',
          background: `${color}20`,
          border: `1px solid ${color}40`,
          borderRadius: '12px',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
        }}>
          <Icon size={20} color={color} />
        </div>
      </div>
    </div>
  );
}

export default function DashboardPage() {
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);
  const user = useAuthStore((s) => s.user);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const analyticsRes = await analyticsApi.getSummary();
        setAnalytics(analyticsRes.data.data);
      } catch (e) {
        console.error(e);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  if (loading) {
    return (
      <div>
        <div className="page-header">
          <div className="skeleton" style={{ height: '32px', width: '250px', marginBottom: '8px' }} />
          <div className="skeleton" style={{ height: '18px', width: '180px' }} />
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
          {[...Array(4)].map((_, i) => <div key={i} className="skeleton" style={{ height: '110px', borderRadius: '12px' }} />)}
        </div>
      </div>
    );
  }

  return (
    <div className="fade-in">
      {/* Header */}
      <div className="page-header">
        <h1 className="page-title">
          Good {new Date().getHours() < 12 ? 'morning' : new Date().getHours() < 18 ? 'afternoon' : 'evening'},{' '}
          <span className="gradient-text">{user?.username}</span> 👋
        </h1>
        <p className="page-subtitle">Here's your job search summary</p>
      </div>

      {/* Stats grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem', marginBottom: '2rem' }}>
        <StatCard icon={Briefcase} label="Total Applications" value={analytics?.totalApplications ?? 0} color="#6366f1" subtitle="All time" />
        <StatCard icon={TrendingUp} label="Active" value={analytics?.activeApplications ?? 0} color="#3b82f6" subtitle="In progress" />
        <StatCard icon={Award} label="Offers" value={analytics?.offersReceived ?? 0} color="#10b981" subtitle={`${analytics?.offerConversionRate ?? 0}% conversion`} />
        <StatCard icon={Calendar} label="Upcoming" value={analytics?.interviewsScheduled ?? 0} color="#f59e0b" subtitle="Interviews in 30d" />
      </div>

      {/* Status distribution */}
      <div className="card" style={{ maxWidth: '600px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
          <h2 style={{ fontSize: '1rem', fontWeight: 700, color: 'var(--text-primary)' }}>Status Breakdown</h2>
          <Link to="/applications" style={{ color: 'var(--brand-color)', fontSize: '0.8rem', textDecoration: 'none', display: 'flex', alignItems: 'center', gap: '4px' }}>
            Applications <ArrowRight size={14} />
          </Link>
        </div>
        {analytics?.statusDistribution && Object.entries(analytics.statusDistribution).map(([status, count]) => (
          <div key={status} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.75rem 0', borderBottom: '1px solid var(--border-color)' }}>
            <span style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', fontWeight: 500 }}>{status}</span>
            <span style={{ fontWeight: 700, color: 'var(--text-primary)', background: 'var(--bg-secondary)', padding: '4px 12px', borderRadius: '20px', fontSize: '0.85rem' }}>{count}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
