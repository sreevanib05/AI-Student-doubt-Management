import { Plus, RefreshCw, Users } from 'lucide-react';
import { useEffect, useState } from 'react';
import EmptyState from '../../components/EmptyState.jsx';
import ErrorAlert from '../../components/ErrorAlert.jsx';
import PageHeader from '../../components/PageHeader.jsx';
import { adminService } from '../../services/adminService.js';
import { mentorService } from '../../services/mentorService.js';

const initialForm = {
  name: '',
  email: '',
  password: '',
  expertise: '',
};

export default function MentorManagementPage() {
  const [mentors, setMentors] = useState([]);
  const [form, setForm] = useState(initialForm);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadMentors();
  }, []);

  function loadMentors() {
    setError('');
    mentorService.all()
      .then(setMentors)
      .catch((exception) => setError(exception.response?.data?.message || 'Could not load mentors.'));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setMessage('');
    setLoading(true);

    try {
      await adminService.createMentor(form);
      setForm(initialForm);
      setMessage('Mentor account created. The mentor can now sign in from the login page.');
      loadMentors();
    } catch (exception) {
      setError(exception.response?.data?.message || 'Could not create mentor.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <>
      <PageHeader
        title="Mentors"
        description="Create mentor login accounts and monitor active mentors."
        action={
          <button type="button" className="secondary-button" onClick={loadMentors}>
            <RefreshCw size={17} />
            Refresh
          </button>
        }
      />

      <div className="grid grid-cols-[420px_minmax(0,1fr)] gap-6">
        <form onSubmit={handleSubmit} className="rounded-lg border border-slate-200 bg-white p-5 shadow-dashboard">
          <div className="mb-4 flex items-center gap-2">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-blue-50 text-primary">
              <Plus size={18} />
            </div>
            <h2 className="text-base font-bold text-slate-950">Create Mentor</h2>
          </div>

          <div className="space-y-4">
            <ErrorAlert message={error} />
            {message && <div className="rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">{message}</div>}

            <div>
              <label className="field-label" htmlFor="mentor-name">Name</label>
              <input
                id="mentor-name"
                className="field-input mt-1"
                value={form.name}
                onChange={(event) => setForm({ ...form, name: event.target.value })}
                required
              />
            </div>

            <div>
              <label className="field-label" htmlFor="mentor-email">Email</label>
              <input
                id="mentor-email"
                className="field-input mt-1"
                type="email"
                value={form.email}
                onChange={(event) => setForm({ ...form, email: event.target.value })}
                required
              />
            </div>

            <div>
              <label className="field-label" htmlFor="mentor-password">Password</label>
              <input
                id="mentor-password"
                className="field-input mt-1"
                type="password"
                minLength={6}
                value={form.password}
                onChange={(event) => setForm({ ...form, password: event.target.value })}
                required
              />
            </div>

            <div>
              <label className="field-label" htmlFor="mentor-expertise">Expertise</label>
              <input
                id="mentor-expertise"
                className="field-input mt-1"
                value={form.expertise}
                onChange={(event) => setForm({ ...form, expertise: event.target.value })}
                placeholder="Coding Java"
                required
              />
            </div>

            <button type="submit" className="primary-button w-full" disabled={loading}>
              <Users size={17} />
              {loading ? 'Creating...' : 'Create Mentor'}
            </button>
          </div>
        </form>

        <section>
          <h2 className="mb-3 text-lg font-bold text-slate-950">Active Mentors</h2>
          {mentors.length > 0 ? (
            <div className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-dashboard">
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
                  <tr>
                    <th className="px-4 py-3">Name</th>
                    <th className="px-4 py-3">Email</th>
                    <th className="px-4 py-3">Expertise</th>
                    <th className="px-4 py-3">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {mentors.map((mentor) => (
                    <tr key={mentor.id}>
                      <td className="px-4 py-4 font-semibold text-slate-900">{mentor.name}</td>
                      <td className="px-4 py-4 text-slate-600">{mentor.email}</td>
                      <td className="px-4 py-4 text-slate-600">{mentor.expertise}</td>
                      <td className="px-4 py-4 text-slate-600">{mentor.active ? 'Active' : 'Inactive'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <EmptyState title="No mentors found" description="Create a mentor account to begin assigning doubts." />
          )}
        </section>
      </div>
    </>
  );
}
