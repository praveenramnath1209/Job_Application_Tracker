import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { FileSearch, Loader2, CheckCircle, XCircle, Lightbulb, Target } from 'lucide-react';
import toast from 'react-hot-toast';
import { analyzerApi } from '../api/analyzer';
import { getErrorMessage } from '../utils/formatters';

const schema = z.object({
  jobDescription: z.string().min(50, 'Min 50 characters').max(10000),
  resumeText: z.string().max(10000).optional(),
});

function MatchMeter({ percentage }) {
  const color = percentage >= 70 ? '#10b981' : percentage >= 50 ? '#f59e0b' : '#ef4444';
  return (
    <div style={{ textAlign: 'center', margin: '0 auto', width: '160px' }}>
      <div style={{ position: 'relative', width: '160px', height: '160px' }}>
        <svg width="160" height="160" style={{ transform: 'rotate(-90deg)' }}>
          <circle cx="80" cy="80" r="65" fill="none" stroke="#334155" strokeWidth="12" />
          <circle
            cx="80" cy="80" r="65" fill="none" stroke={color} strokeWidth="12"
            strokeDasharray={`${2 * Math.PI * 65}`}
            strokeDashoffset={`${2 * Math.PI * 65 * (1 - percentage / 100)}`}
            strokeLinecap="round"
            style={{ transition: 'stroke-dashoffset 1s ease' }}
          />
        </svg>
        <div style={{
          position: 'absolute', inset: 0, display: 'flex', flexDirection: 'column',
          alignItems: 'center', justifyContent: 'center',
        }}>
          <span style={{ fontSize: '2rem', fontWeight: 800, color }}>{percentage.toFixed(0)}%</span>
          <span style={{ fontSize: '0.7rem', color: '#64748b' }}>Match</span>
        </div>
      </div>
    </div>
  );
}

export default function AnalyzerPage() {
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [resumeFile, setResumeFile] = useState(null);
  const [inputType, setInputType] = useState('text'); // 'text' or 'file'

  const { register, handleSubmit, formState: { errors }, setError, clearErrors } = useForm({
    resolver: zodResolver(schema),
  });

  const onSubmit = async (data) => {
    if (inputType === 'text' && (!data.resumeText || data.resumeText.length < 50)) {
      setError('resumeText', { type: 'manual', message: 'Min 50 characters required' });
      return;
    }
    if (inputType === 'file' && !resumeFile) {
      toast.error('Please select a PDF file');
      return;
    }

    setLoading(true);
    setResult(null);
    try {
      let res;
      if (inputType === 'file') {
        const formData = new FormData();
        formData.append('file', resumeFile);
        formData.append('jobDescription', data.jobDescription);
        res = await analyzerApi.analyzePdf(formData);
      } else {
        res = await analyzerApi.analyze(data);
      }
      setResult(res.data.data);
    } catch (err) {
      toast.error(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fade-in">
      <div className="page-header">
        <h1 className="page-title">Resume Analyzer</h1>
        <p className="page-subtitle">Analyze how well your resume matches a job description</p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: result ? '1fr 1fr' : '1fr', gap: '1.5rem' }}>
        {/* Input form */}
        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.25rem' }}>
            <FileSearch size={20} color="#818cf8" />
            <h2 style={{ fontSize: '1rem', fontWeight: 700, color: '#f1f5f9' }}>Input</h2>
          </div>

          <form onSubmit={handleSubmit(onSubmit)} style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
            <div>
              <label className="label">Job Description</label>
              <textarea {...register('jobDescription')} className="input-field" rows={10}
                placeholder="Paste the full job description here..."
                style={{ resize: 'vertical', fontFamily: 'inherit', fontSize: '0.8rem' }} />
              {errors.jobDescription && <p className="error-text">{errors.jobDescription.message}</p>}
            </div>

            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                <label className="label" style={{ marginBottom: 0 }}>Your Resume / Profile</label>
                <div style={{ display: 'flex', gap: '0.5rem', background: 'var(--bg-secondary)', padding: '2px', borderRadius: '6px' }}>
                  <button type="button" onClick={() => { setInputType('text'); clearErrors('resumeText'); }}
                    style={{ padding: '4px 8px', fontSize: '0.75rem', borderRadius: '4px', border: 'none', cursor: 'pointer', background: inputType === 'text' ? 'var(--bg-card)' : 'transparent', color: inputType === 'text' ? 'var(--text-primary)' : 'var(--text-muted)' }}>
                    Paste Text
                  </button>
                  <button type="button" onClick={() => { setInputType('file'); clearErrors('resumeText'); }}
                    style={{ padding: '4px 8px', fontSize: '0.75rem', borderRadius: '4px', border: 'none', cursor: 'pointer', background: inputType === 'file' ? 'var(--bg-card)' : 'transparent', color: inputType === 'file' ? 'var(--text-primary)' : 'var(--text-muted)' }}>
                    Upload PDF
                  </button>
                </div>
              </div>

              {inputType === 'text' ? (
                <>
                  <textarea {...register('resumeText')} className="input-field" rows={10}
                    placeholder="Paste your resume text or LinkedIn profile summary..."
                    style={{ resize: 'vertical', fontFamily: 'inherit', fontSize: '0.8rem' }} />
                  {errors.resumeText && <p className="error-text">{errors.resumeText.message}</p>}
                </>
              ) : (
                <div style={{ 
                  border: '2px dashed var(--border-color)', borderRadius: '8px', padding: '2rem', textAlign: 'center',
                  background: 'var(--bg-secondary)', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '1rem'
                }}>
                  <input 
                    type="file" 
                    accept=".pdf" 
                    onChange={(e) => setResumeFile(e.target.files[0])} 
                    style={{ display: 'none' }} 
                    id="resume-upload" 
                  />
                  <label htmlFor="resume-upload" className="btn-secondary" style={{ cursor: 'pointer' }}>
                    Choose PDF File
                  </label>
                  {resumeFile && <p style={{ color: 'var(--brand-color)', fontSize: '0.85rem' }}>{resumeFile.name}</p>}
                </div>
              )}
            </div>

            <button type="submit" className="btn-primary" disabled={loading}
              style={{ width: '100%', justifyContent: 'center', padding: '0.75rem' }}>
              {loading ? <Loader2 size={18} /> : <Target size={18} />}
              {loading ? 'Analyzing...' : 'Analyze Match'}
            </button>
          </form>
        </div>

        {/* Results */}
        {result && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {/* Match score */}
            <div className="card" style={{ textAlign: 'center' }}>
              <MatchMeter percentage={result.matchPercentage} />
              <p style={{ color: '#f1f5f9', fontWeight: 700, marginTop: '0.75rem', fontSize: '1rem' }}>{result.overallFeedback}</p>
              <p style={{ color: '#64748b', fontSize: '0.8rem', marginTop: '0.25rem' }}>
                {result.matchedKeywords} / {result.totalJobKeywords} skills matched
              </p>
            </div>

            {/* Matched skills */}
            {result.matchedSkills?.length > 0 && (
              <div className="card">
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.75rem' }}>
                  <CheckCircle size={16} color="#10b981" />
                  <h3 style={{ fontSize: '0.9rem', fontWeight: 700, color: '#f1f5f9' }}>Matched Skills</h3>
                </div>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.4rem' }}>
                  {result.matchedSkills.map((skill) => (
                    <span key={skill} style={{
                      background: 'rgba(16,185,129,0.1)', border: '1px solid rgba(16,185,129,0.3)',
                      color: '#34d399', padding: '3px 10px', borderRadius: '20px', fontSize: '0.75rem', fontWeight: 600,
                    }}>{skill}</span>
                  ))}
                </div>
              </div>
            )}

            {/* Missing skills */}
            {result.missingSkills?.length > 0 && (
              <div className="card">
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.75rem' }}>
                  <XCircle size={16} color="#ef4444" />
                  <h3 style={{ fontSize: '0.9rem', fontWeight: 700, color: '#f1f5f9' }}>Missing Skills</h3>
                </div>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.4rem' }}>
                  {result.missingSkills.map((skill) => (
                    <span key={skill} style={{
                      background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.3)',
                      color: '#f87171', padding: '3px 10px', borderRadius: '20px', fontSize: '0.75rem', fontWeight: 600,
                    }}>{skill}</span>
                  ))}
                </div>
              </div>
            )}

            {/* Recommendations */}
            {result.recommendations?.length > 0 && (
              <div className="card">
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.75rem' }}>
                  <Lightbulb size={16} color="#f59e0b" />
                  <h3 style={{ fontSize: '0.9rem', fontWeight: 700, color: '#f1f5f9' }}>Recommendations</h3>
                </div>
                <ol style={{ paddingLeft: '1.25rem', display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                  {result.recommendations.map((rec, i) => (
                    <li key={i} style={{ color: '#94a3b8', fontSize: '0.825rem', lineHeight: 1.5 }}>{rec}</li>
                  ))}
                </ol>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
