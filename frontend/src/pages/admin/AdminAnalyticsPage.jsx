import { BookOpen, Clock3, FileText, Play, RefreshCw } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import AdminDoubtAssignmentTable from '../../components/AdminDoubtAssignmentTable.jsx';
import EmptyState from '../../components/EmptyState.jsx';
import ErrorAlert from '../../components/ErrorAlert.jsx';
import PageHeader from '../../components/PageHeader.jsx';
import StatCard from '../../components/StatCard.jsx';
import { getApiErrorMessage } from '../../services/api.js';
import { adminService } from '../../services/adminService.js';
import { doubtService } from '../../services/doubtService.js';
import { mentorService } from '../../services/mentorService.js';

const categories = ['ALL', 'CONCEPTUAL', 'CODING', 'DEBUGGING'];

export default function AdminAnalyticsPage() {
  const [analytics, setAnalytics] = useState(null);
  const [doubts, setDoubts] = useState([]);
  const [mentors, setMentors] = useState([]);
  const [category, setCategory] = useState('ALL');
  const [simulation, setSimulation] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadAnalytics();
    loadMentors();
  }, []);

  useEffect(() => {
    loadDoubts();
  }, [category]);

  function loadAnalytics() {
    adminService.analytics()
      .then(setAnalytics)
      .catch((exception) => setError(getApiErrorMessage(exception, 'Could not load analytics.')));
  }

  function loadDoubts() {
    const request = category === 'ALL' ? doubtService.all() : doubtService.byCategory(category);

    request
      .then(setDoubts)
      .catch((exception) => setError(getApiErrorMessage(exception, 'Could not load doubts.')));
  }

  function loadMentors() {
    mentorService.all()
      .then(setMentors)
      .catch((exception) => setError(getApiErrorMessage(exception, 'Could not load mentors.')));
  }

  function reloadAll() {
    loadAnalytics();
    loadMentors();
    loadDoubts();
  }

  async function runSimulation() {
    setLoading(true);
    setError('');

    try {
      const result = await adminService.simulateMentors();
      setSimulation(result.messages);
    } catch (exception) {
      setError(getApiErrorMessage(exception, 'Could not run simulation.'));
    } finally {
      setLoading(false);
    }
  }

  const maxCategoryTotal = useMemo(() => {
    if (!analytics?.categoryStats?.length) {
      return 1;
    }

    return Math.max(...analytics.categoryStats.map((item) => item.total));
  }, [analytics]);

  const maxSubjectTotal = useMemo(() => {
    if (!analytics?.subjectStats?.length) {
      return 1;
    }

    return Math.max(...analytics.subjectStats.map((item) => item.total));
  }, [analytics]);

  const averageResolutionHours = useMemo(() => {
    const value = analytics?.averageResolutionHours || 0;
    return value > 0 ? `${value.toFixed(1)}h` : '0h';
  }, [analytics]);

  return (
    <>
      <PageHeader
        title="Teacher Analytics"
        description="Filter doubts by category and review subject, context, file, and mentor signals."
        action={
          <div className="flex items-center gap-3">
            <button type="button" className="secondary-button" onClick={reloadAll}>
              <RefreshCw size={17} />
              Refresh
            </button>
            <button type="button" className="primary-button" onClick={runSimulation} disabled={loading}>
              <Play size={17} />
              {loading ? 'Running...' : 'Run Mentor Threads'}
            </button>
          </div>
        }
      />

      <ErrorAlert message={error} />

      <div className="mt-6 grid grid-cols-4 gap-5">
        <StatCard label="PDF Doubts" value={analytics?.pdfAttachmentCount ?? 0} icon={FileText} tone="blue" />
        <StatCard label="Context Notes" value={analytics?.contextRichDoubts ?? 0} icon={BookOpen} tone="green" />
        <StatCard label="Avg Resolution" value={averageResolutionHours} icon={Clock3} tone="amber" />
        <StatCard label="Active Doubts" value={(analytics?.openDoubts ?? 0) + (analytics?.assignedDoubts ?? 0) + (analytics?.inProgressDoubts ?? 0)} icon={RefreshCw} tone="slate" />
      </div>

      <div className="mt-6 grid grid-cols-[420px_minmax(0,1fr)] gap-6">
        <section className="rounded-lg border border-slate-200 bg-white p-5 shadow-dashboard">
          <h2 className="text-base font-bold text-slate-950">Category Volume</h2>
          <div className="mt-5 space-y-4">
            {analytics?.categoryStats?.length > 0 ? (
              analytics.categoryStats.map((item) => (
                <div key={item.category}>
                  <div className="mb-1 flex items-center justify-between text-sm">
                    <span className="font-medium text-slate-700">{item.category}</span>
                    <span className="text-slate-500">{item.total}</span>
                  </div>
                  <div className="h-3 rounded-lg bg-slate-100">
                    <div
                      className="h-3 rounded-lg bg-primary"
                      style={{ width: `${Math.max((item.total / maxCategoryTotal) * 100, 8)}%` }}
                    />
                  </div>
                </div>
              ))
            ) : (
              <p className="text-sm text-slate-500">No category data yet.</p>
            )}
          </div>

          <h2 className="mt-8 text-base font-bold text-slate-950">Subject Signals</h2>
          <div className="mt-5 space-y-4">
            {analytics?.subjectStats?.length > 0 ? (
              analytics.subjectStats.map((item) => (
                <div key={item.subject}>
                  <div className="mb-1 flex items-center justify-between text-sm">
                    <span className="font-medium text-slate-700">{item.subject}</span>
                    <span className="text-slate-500">{item.total}</span>
                  </div>
                  <div className="h-3 rounded-lg bg-slate-100">
                    <div
                      className="h-3 rounded-lg bg-emerald-500"
                      style={{ width: `${Math.max((item.total / maxSubjectTotal) * 100, 8)}%` }}
                    />
                  </div>
                  <div className="mt-1 flex items-center justify-between text-xs text-slate-500">
                    <span>{item.active} active</span>
                    <span>{item.resolved} resolved</span>
                  </div>
                </div>
              ))
            ) : (
              <p className="text-sm text-slate-500">No subject data yet.</p>
            )}
          </div>

          <h2 className="mt-8 text-base font-bold text-slate-950">Mentor Monitoring</h2>
          <div className="mt-4 space-y-3">
            {analytics?.mentorWorkloads?.map((mentor) => (
              <div key={mentor.mentorId} className="rounded-lg border border-slate-200 p-3">
                <div className="flex items-center justify-between gap-3">
                  <div>
                    <p className="text-sm font-semibold text-slate-900">{mentor.mentorName}</p>
                    <p className="text-xs text-slate-500">{mentor.expertise}</p>
                  </div>
                  <div className="text-right text-xs text-slate-500">
                    <p>{mentor.assigned} active</p>
                    <p>{mentor.resolved} resolved</p>
                  </div>
                </div>
              </div>
            ))}
          </div>

          {simulation.length > 0 && (
            <div className="mt-6 rounded-lg border border-blue-100 bg-blue-50 p-3">
              <p className="mb-2 text-sm font-semibold text-primary">Thread Simulation</p>
              <div className="space-y-1">
                {simulation.map((item) => (
                  <p key={item} className="text-xs text-slate-600">{item}</p>
                ))}
              </div>
            </div>
          )}
        </section>

        <section>
          <div className="mb-3 flex items-center justify-between">
            <h2 className="text-lg font-bold text-slate-950">Doubts</h2>
            <select className="field-input w-52" value={category} onChange={(event) => setCategory(event.target.value)}>
              {categories.map((item) => (
                <option key={item} value={item}>{item}</option>
              ))}
            </select>
          </div>

          {doubts.length > 0 ? (
            <AdminDoubtAssignmentTable doubts={doubts} mentors={mentors} onChanged={reloadAll} />
          ) : (
            <EmptyState title="No doubts found" description="Try another category after students submit doubts." />
          )}
        </section>
      </div>
    </>
  );
}
