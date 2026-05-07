import { CheckCircle2, Inbox, Timer } from 'lucide-react';
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import DoubtTable from '../../components/DoubtTable.jsx';
import EmptyState from '../../components/EmptyState.jsx';
import ErrorAlert from '../../components/ErrorAlert.jsx';
import PageHeader from '../../components/PageHeader.jsx';
import StatCard from '../../components/StatCard.jsx';
import { doubtService } from '../../services/doubtService.js';

export default function MentorDashboard() {
  const [doubts, setDoubts] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    doubtService.assigned()
      .then(setDoubts)
      .catch((exception) => setError(exception.response?.data?.message || 'Could not load assigned doubts.'));
  }, []);

  const active = doubts.filter((doubt) => doubt.status !== 'RESOLVED').length;
  const resolved = doubts.filter((doubt) => doubt.status === 'RESOLVED').length;

  return (
    <>
      <PageHeader
        title="Mentor Dashboard"
        description="Review assigned doubts and submit responses."
        action={
          <Link to="/mentor/assigned" className="primary-button">
            <Inbox size={17} />
            Assigned Doubts
          </Link>
        }
      />

      <ErrorAlert message={error} />

      <div className="mt-6 grid grid-cols-3 gap-5">
        <StatCard label="Assigned to You" value={doubts.length} icon={Inbox} tone="blue" />
        <StatCard label="Active Work" value={active} icon={Timer} tone="amber" />
        <StatCard label="Resolved" value={resolved} icon={CheckCircle2} tone="green" />
      </div>

      <section className="mt-7">
        <h2 className="mb-3 text-lg font-bold text-slate-950">Latest Assigned Doubts</h2>
        {doubts.length > 0 ? (
          <DoubtTable doubts={doubts.slice(0, 5)} />
        ) : (
          <EmptyState title="No assigned doubts" description="Newly assigned student doubts will appear here." />
        )}
      </section>
    </>
  );
}
