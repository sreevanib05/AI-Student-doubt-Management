import { CheckCircle2, Clock3, History, Send } from 'lucide-react';
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import DoubtTable from '../../components/DoubtTable.jsx';
import EmptyState from '../../components/EmptyState.jsx';
import ErrorAlert from '../../components/ErrorAlert.jsx';
import PageHeader from '../../components/PageHeader.jsx';
import StatCard from '../../components/StatCard.jsx';
import { getApiErrorMessage } from '../../services/api.js';
import { doubtService } from '../../services/doubtService.js';

export default function StudentDashboard() {
  const [doubts, setDoubts] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    doubtService.myDoubts()
      .then(setDoubts)
      .catch((exception) => setError(getApiErrorMessage(exception, 'Could not load dashboard.')));
  }, []);

  const resolved = doubts.filter((doubt) => doubt.status === 'RESOLVED').length;
  const active = doubts.filter((doubt) => doubt.status !== 'RESOLVED').length;
  const recent = doubts.slice(0, 5);

  return (
    <>
      <PageHeader
        title="Student Dashboard"
        description="Track submitted doubts, mentor assignment, and resolved answers."
        action={
          <Link to="/student/submit" className="primary-button">
            <Send size={17} />
            Submit Doubt
          </Link>
        }
      />

      <ErrorAlert message={error} />

      <div className="mt-6 grid grid-cols-3 gap-5">
        <StatCard label="Total Doubts" value={doubts.length} icon={History} tone="blue" />
        <StatCard label="Active Doubts" value={active} icon={Clock3} tone="amber" />
        <StatCard label="Resolved Doubts" value={resolved} icon={CheckCircle2} tone="green" />
      </div>

      <section className="mt-7">
        <div className="mb-3 flex items-center justify-between">
          <h2 className="text-lg font-bold text-slate-950">Recent Doubts</h2>
          <Link to="/student/doubts" className="text-sm font-semibold text-primary hover:text-blue-700">
            View all
          </Link>
        </div>
        {recent.length > 0 ? (
          <DoubtTable doubts={recent} />
        ) : (
          <EmptyState title="No doubts submitted yet" description="Your submitted doubts will appear here." />
        )}
      </section>
    </>
  );
}
