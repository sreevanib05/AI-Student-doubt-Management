import { CheckCircle2, RefreshCw, Save } from 'lucide-react';
import { useEffect, useState } from 'react';
import EmptyState from '../../components/EmptyState.jsx';
import ErrorAlert from '../../components/ErrorAlert.jsx';
import PageHeader from '../../components/PageHeader.jsx';
import StatusBadge from '../../components/StatusBadge.jsx';
import { getApiErrorMessage } from '../../services/api.js';
import { doubtService } from '../../services/doubtService.js';
import { mentorService } from '../../services/mentorService.js';

export default function AssignedDoubtsPage() {
  const [doubts, setDoubts] = useState([]);
  const [responses, setResponses] = useState({});
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [loadingId, setLoadingId] = useState(null);

  useEffect(() => {
    loadDoubts();
  }, []);

  function loadDoubts() {
    setError('');
    doubtService.assigned()
      .then(setDoubts)
      .catch((exception) => setError(getApiErrorMessage(exception, 'Could not load assigned doubts.')));
  }

  async function updateStatus(doubtId, status) {
    setLoadingId(doubtId);
    setError('');
    setMessage('');

    try {
      await doubtService.updateStatus(doubtId, status);
      setMessage('Status updated.');
      loadDoubts();
    } catch (exception) {
      setError(getApiErrorMessage(exception, 'Could not update status.'));
    } finally {
      setLoadingId(null);
    }
  }

  async function submitResponse(doubtId) {
    setLoadingId(doubtId);
    setError('');
    setMessage('');

    try {
      await mentorService.respond({ doubtId, responseText: responses[doubtId] || '' });
      setResponses({ ...responses, [doubtId]: '' });
      setMessage('Response submitted and doubt marked resolved.');
      loadDoubts();
    } catch (exception) {
      setError(getApiErrorMessage(exception, 'Could not submit response.'));
    } finally {
      setLoadingId(null);
    }
  }

  return (
    <>
      <PageHeader
        title="Assigned Doubts"
        description="Resolve student doubts and update progress."
        action={
          <button type="button" onClick={loadDoubts} className="secondary-button">
            <RefreshCw size={17} />
            Refresh
          </button>
        }
      />

      <div className="space-y-3">
        <ErrorAlert message={error} />
        {message && <div className="rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">{message}</div>}
      </div>

      <div className="mt-6 space-y-4">
        {doubts.length === 0 && <EmptyState title="No assigned doubts" description="There is nothing assigned to this mentor yet." />}

        {doubts.map((doubt) => (
          <article key={doubt.id} className="rounded-lg border border-slate-200 bg-white p-5 shadow-dashboard">
            <div className="flex items-start justify-between gap-5">
              <div>
                <div className="mb-2 flex items-center gap-3">
                  <h2 className="text-lg font-bold text-slate-950">{doubt.title}</h2>
                  <StatusBadge status={doubt.status} />
                </div>
                <p className="text-sm font-medium text-primary">{doubt.category}</p>
                <p className="mt-3 max-w-3xl text-sm leading-6 text-slate-600">{doubt.description}</p>
                <p className="mt-3 text-xs text-slate-500">Student: {doubt.studentName}</p>
              </div>

              <div className="flex min-w-48 flex-col gap-2">
                <button
                  type="button"
                  onClick={() => updateStatus(doubt.id, 'IN_PROGRESS')}
                  className="secondary-button"
                  disabled={loadingId === doubt.id || doubt.status === 'RESOLVED'}
                >
                  <Save size={16} />
                  In Progress
                </button>
                <button
                  type="button"
                  onClick={() => updateStatus(doubt.id, 'RESOLVED')}
                  className="secondary-button"
                  disabled={loadingId === doubt.id}
                >
                  <CheckCircle2 size={16} />
                  Mark Resolved
                </button>
              </div>
            </div>

            <div className="mt-5 grid grid-cols-[minmax(0,1fr)_180px] gap-3">
              <textarea
                className="field-input min-h-24 resize-y"
                value={responses[doubt.id] || ''}
                onChange={(event) => setResponses({ ...responses, [doubt.id]: event.target.value })}
                placeholder="Write the mentor response"
              />
              <button
                type="button"
                onClick={() => submitResponse(doubt.id)}
                className="primary-button self-start"
                disabled={loadingId === doubt.id}
              >
                <CheckCircle2 size={17} />
                Respond
              </button>
            </div>

            {doubt.latestResponse && (
              <div className="mt-4 rounded-lg bg-slate-50 p-3 text-sm text-slate-600">
                <span className="font-semibold text-slate-800">Latest response:</span> {doubt.latestResponse}
              </div>
            )}
          </article>
        ))}
      </div>
    </>
  );
}
