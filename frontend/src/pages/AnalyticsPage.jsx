import React, { useEffect, useState } from 'react';
import {
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend, FunnelChart, Funnel, LabelList,
} from 'recharts';
import { analyticsApi } from '../api/analytics';
import { CHART_COLORS } from '../utils/constants';
import { getErrorMessage } from '../utils/formatters';
import toast from 'react-hot-toast';

const CustomTooltip = ({ active, payload, label }) => {
  if (active && payload && payload.length) {
    return (
      <div style={{ background: 'var(--bg-card)', border: '1px solid var(--border-color)', borderRadius: '8px', padding: '8px 12px' }}>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.75rem' }}>{label}</p>
        <p style={{ color: 'var(--text-primary)', fontWeight: 700 }}>{payload[0].value}</p>
      </div>
    );
  }
  return null;
};

export default function AnalyticsPage() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    analyticsApi.getSummary()
      .then((res) => setData(res.data.data))
      .catch((err) => toast.error(getErrorMessage(err)))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div>
        <div className="skeleton" style={{ height: '32px', width: '200px', marginBottom: '2rem' }} />
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
          {[...Array(4)].map((_, i) => <div key={i} className="skeleton" style={{ height: '280px', borderRadius: '12px' }} />)}
        </div>
      </div>
    );
  }

  const statusData = data?.statusDistribution
    ? Object.entries(data.statusDistribution).map(([key, value]) => ({
        name: key, value, fill: CHART_COLORS[Object.keys(data.statusDistribution).indexOf(key) % CHART_COLORS.length],
      }))
    : [];

  const funnelData = data?.statusDistribution ? 
    [
      { name: 'Applied', value: data.statusDistribution['Applied'] || 0, fill: '#6366f1' },
      { name: 'Shortlisted', value: data.statusDistribution['Shortlisted'] || 0, fill: '#f59e0b' },
      { name: 'Offer Received', value: data.statusDistribution['Offer Received'] || 0, fill: '#10b981' },
    ].filter(d => d.value > 0)
    : [];

  const monthlyData = data?.monthlyApplications?.map(m => ({ month: m.month, count: m.count })) || [];
  const ctcData = data?.ctcDistribution?.filter(d => d.count > 0) || [];

  return (
    <div className="fade-in">
      <div className="page-header">
        <h1 className="page-title">Analytics Dashboard</h1>
        <p className="page-subtitle">Track your job search performance</p>
      </div>

      {/* KPI Row */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))', gap: '1rem', marginBottom: '2rem' }}>
        {[
          { label: 'Total Applications', value: data?.totalApplications ?? 0 },
          { label: 'Offer Conversion', value: `${data?.offerConversionRate ?? 0}%` },
          { label: 'Interview Success', value: `${data?.interviewSuccessRate ?? 0}%` },
          { label: 'Offers Received', value: data?.offersReceived ?? 0 },
        ].map(({ label, value }) => (
          <div key={label} className="card" style={{ textAlign: 'center' }}>
            <p style={{ color: '#64748b', fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>{label}</p>
            <p style={{ fontSize: '2rem', fontWeight: 800, color: '#818cf8', marginTop: '0.25rem' }}>{value}</p>
          </div>
        ))}
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
        {/* Monthly Applications Bar Chart */}
        <div className="card">
          <h3 style={{ fontSize: '0.95rem', fontWeight: 700, color: '#f1f5f9', marginBottom: '1.25rem' }}>Monthly Applications</h3>
          {monthlyData.length === 0 ? (
            <div style={{ height: '200px', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#64748b' }}>No data yet</div>
          ) : (
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={monthlyData}>
                <XAxis dataKey="month" tick={{ fill: '#64748b', fontSize: 11 }} axisLine={false} tickLine={false} />
                <YAxis tick={{ fill: '#64748b', fontSize: 11 }} axisLine={false} tickLine={false} allowDecimals={false} />
                <Tooltip content={<CustomTooltip />} />
                <Bar dataKey="count" fill="#6366f1" radius={[4, 4, 0, 0]} maxBarSize={40} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>

        {/* Status Pie Chart */}
        <div className="card">
          <h3 style={{ fontSize: '0.95rem', fontWeight: 700, color: '#f1f5f9', marginBottom: '1.25rem' }}>Status Distribution</h3>
          {statusData.every(d => d.value === 0) ? (
            <div style={{ height: '220px', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#64748b' }}>No data yet</div>
          ) : (
            <ResponsiveContainer width="100%" height={220}>
              <PieChart>
                <Pie data={statusData} cx="50%" cy="50%" outerRadius={80} dataKey="value" nameKey="name">
                  {statusData.map((entry, index) => (
                    <Cell key={index} fill={entry.fill} />
                  ))}
                </Pie>
                <Tooltip content={<CustomTooltip />} />
                <Legend iconType="circle" iconSize={8} wrapperStyle={{ fontSize: '0.75rem', color: '#94a3b8' }} />
              </PieChart>
            </ResponsiveContainer>
          )}
        </div>

        {/* Application Funnel */}
        <div className="card">
          <h3 style={{ fontSize: '0.95rem', fontWeight: 700, color: '#f1f5f9', marginBottom: '1.25rem' }}>Application Funnel</h3>
          {funnelData.length === 0 ? (
            <div style={{ height: '220px', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#64748b' }}>No data yet</div>
          ) : (
            <ResponsiveContainer width="100%" height={220}>
              <FunnelChart>
                <Tooltip content={<CustomTooltip />} />
                <Funnel dataKey="value" data={funnelData} isAnimationActive>
                  <LabelList position="center" fill="#fff" fontSize={11} dataKey="name" />
                </Funnel>
              </FunnelChart>
            </ResponsiveContainer>
          )}
        </div>

        {/* CTC Distribution */}
        <div className="card">
          <h3 style={{ fontSize: '0.95rem', fontWeight: 700, color: '#f1f5f9', marginBottom: '1.25rem' }}>CTC Distribution</h3>
          {ctcData.length === 0 ? (
            <div style={{ height: '220px', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#64748b' }}>Add CTC data to applications</div>
          ) : (
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={ctcData} layout="vertical">
                <XAxis type="number" tick={{ fill: '#64748b', fontSize: 11 }} axisLine={false} tickLine={false} allowDecimals={false} />
                <YAxis type="category" dataKey="range" tick={{ fill: '#94a3b8', fontSize: 11 }} axisLine={false} tickLine={false} width={80} />
                <Tooltip content={<CustomTooltip />} />
                <Bar dataKey="count" fill="#10b981" radius={[0, 4, 4, 0]} maxBarSize={24} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>
    </div>
  );
}
