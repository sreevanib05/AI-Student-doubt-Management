import { Save, UserMinus } from 'lucide-react';
import { useState } from 'react';
import { getApiErrorMessage } from '../services/api.js';
import { adminService } from '../services/adminService.js';
import ErrorAlert from './ErrorAlert.jsx';
import StatusBadge from './StatusBadge.jsx';

export default function AdminDoubtAssignmentTable({ doubts, mentors, onChanged }) {
  const [selectedMentors, setSelectedMentors] = useState({});
  const [loadingId, setLoadingId] = useState(null);
  const [error, setError] = useState('');

  async function assign(doubt) {
    const mentorId = selectedMentors[doubt.id] || doubt.mentorId;

    if (!mentorId) {
      setError('Choose a mentor first.');
      return;
    }

    setError('');
    setLoadingId(doubt.id);

    try {
      await adminService.assignMentor(doubt.id, Number(mentorId));
      onChanged();
    } catch (exception) {
      setError(getApiErrorMessage(exception, 'Could not assign mentor.'));
    } finally {
      setLoadingId(null);
    }
  }

  async function unassign(doubt) {
    setError('');
    setLoadingId(doubt.id);

    try {
      await adminService.unassignMentor(doubt.id);
      onChanged();
    } catch (exception) {
      setError(getApiErrorMessage(exception, 'Could not unassign mentor.'));
    } finally {
      setLoadingId(null);
    }
  }

  return (
    <div className="space-y-3">
      <ErrorAlert message={error} />

      <div className="grid gap-3">
        {doubts.map((doubt) => (
          <article key={doubt.id} className="rounded-lg border border-slate-200 bg-white p-4 shadow-dashboard">
            <div className="grid grid-cols-12 items-start gap-4">
              <div className="col-span-12 min-w-0 xl:col-span-5">
                <p className="break-words text-base font-bold text-slate-950">{doubt.title}</p>
                <p className="mt-1 line-clamp-2 break-words text-sm leading-6 text-slate-500">{doubt.description}</p>
                <p className="mt-2 text-xs font-medium text-slate-500">Student: {doubt.studentName}</p>
              </div>

              <div className="col-span-4 xl:col-span-2">
                <p className="text-xs font-semibold uppercase tracking-wide text-slate-400">Category</p>
                <p className="mt-2 text-sm font-semibold text-slate-700">{doubt.category}</p>
              </div>

              <div className="col-span-4 xl:col-span-2">
                <p className="text-xs font-semibold uppercase tracking-wide text-slate-400">Status</p>
                <div className="mt-2">
                  <StatusBadge status={doubt.status} />
                </div>
              </div>

              <div className="col-span-4 min-w-0 xl:col-span-3">
                <p className="text-xs font-semibold uppercase tracking-wide text-slate-400">Current Mentor</p>
                <p className="mt-2 break-words text-sm font-semibold text-slate-700">{doubt.mentorName || 'Unassigned'}</p>
              </div>

              <div className="col-span-12">
                <p className="text-xs font-semibold uppercase tracking-wide text-slate-400">Assign Mentor</p>
                <div className="mt-2 flex gap-2">
                  <select
                    className="field-input min-w-0 flex-1"
                    value={selectedMentors[doubt.id] || doubt.mentorId || ''}
                    onChange={(event) => setSelectedMentors({ ...selectedMentors, [doubt.id]: event.target.value })}
                    disabled={doubt.status === 'RESOLVED'}
                  >
                    <option value="">Select mentor</option>
                    {mentors.map((mentor) => (
                      <option key={mentor.id} value={mentor.id}>
                        {mentor.name} - {mentor.expertise}
                      </option>
                    ))}
                  </select>
                  <button
                    type="button"
                    className="primary-button h-[42px] w-10 shrink-0 px-0"
                    title="Save assignment"
                    onClick={() => assign(doubt)}
                    disabled={loadingId === doubt.id || doubt.status === 'RESOLVED'}
                  >
                    <Save size={16} />
                  </button>
                  <button
                    type="button"
                    className="secondary-button h-[42px] w-10 shrink-0 px-0"
                    title="Clear assignment"
                    onClick={() => unassign(doubt)}
                    disabled={loadingId === doubt.id || doubt.status === 'RESOLVED' || !doubt.mentorId}
                  >
                    <UserMinus size={16} />
                  </button>
                </div>
              </div>
            </div>
          </article>
        ))}
      </div>
    </div>
  );
}
