import { BarChart3, CheckCircle2, Clock3, Users } from 'lucide-react';
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import AdminDoubtAssignmentTable from '../../components/AdminDoubtAssignmentTable.jsx';
import EmptyState from '../../components/EmptyState.jsx';
import ErrorAlert from '../../components/ErrorAlert.jsx';
import PageHeader from '../../components/PageHeader.jsx';
import StatCard from '../../components/StatCard.jsx';
import { getApiErrorMessage } from '../../services/api.js';
import { adminService } from '../../services/adminService.js';
import { doubtService } from '../../services/doubtService.js';
import { mentorService } from '../../services/mentorService.js';

export default function AdminDashboard() {
  const [analytics, setAnalytics] = useState(null);
  const [doubts, setDoubts] = useState([]);
  const [mentors, setMentors] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    loadDashboard();
  }, []);

  function loadDashboard() {
    Promise.all([adminService.analytics(), doubtService.all(), mentorService.all()])
      .then(([analyticsData, doubtData, mentorData]) => {
        setAnalytics(analyticsData);
        setDoubts(doubtData);
        setMentors(mentorData);
      })
      .catch((exception) => setError(getApiErrorMessage(exception, 'Could not load admin dashboard.')));
  }

  return (
    <>
      <PageHeader
        title="Admin Dashboard"
        description="Monitor doubt volume, mentor workload, and resolution progress."
        action={
          <Link to="/admin/analytics" className="primary-button">
            <BarChart3 size={17} />
            Analytics
          </Link>
        }
      />

      <ErrorAlert message={error} />

      <div className="mt-6 grid grid-cols-4 gap-5">
        <StatCard label="Total Doubts" value={analytics?.totalDoubts ?? 0} icon={BarChart3} tone="blue" />
        <StatCard label="Active Doubts" value={(analytics?.openDoubts ?? 0) + (analytics?.assignedDoubts ?? 0) + (analytics?.inProgressDoubts ?? 0)} icon={Clock3} tone="amber" />
        <StatCard label="Resolved" value={analytics?.resolvedDoubts ?? 0} icon={CheckCircle2} tone="green" />
        <StatCard label="Mentors" value={analytics?.totalMentors ?? 0} icon={Users} tone="slate" />
      </div>

      <section className="mt-7">
        <h2 className="mb-3 text-lg font-bold text-slate-950">Assign Latest Doubts</h2>
        {doubts.length > 0 ? (
          <AdminDoubtAssignmentTable doubts={doubts.slice(0, 8)} mentors={mentors} onChanged={loadDashboard} />
        ) : (
          <EmptyState title="No doubts yet" description="Student submissions will appear in this dashboard." />
        )}
      </section>
    </>
  );
}
