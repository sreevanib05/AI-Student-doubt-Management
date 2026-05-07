import { Lightbulb, Send } from 'lucide-react';
import { useEffect, useState } from 'react';
import ErrorAlert from '../../components/ErrorAlert.jsx';
import PageHeader from '../../components/PageHeader.jsx';
import { doubtService } from '../../services/doubtService.js';

const initialForm = {
  title: '',
  description: '',
  category: 'CONCEPTUAL',
};

export default function SubmitDoubtPage() {
  const [form, setForm] = useState(initialForm);
  const [suggestions, setSuggestions] = useState([]);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const combinedText = `${form.title} ${form.description}`.trim();

    if (combinedText.length < 12) {
      setSuggestions([]);
      return;
    }

    const timer = window.setTimeout(() => {
      doubtService.suggestions(combinedText)
        .then(setSuggestions)
        .catch(() => setSuggestions([]));
    }, 450);

    return () => window.clearTimeout(timer);
  }, [form.title, form.description]);

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setMessage('');
    setLoading(true);

    try {
      await doubtService.create(form);
      setMessage('Doubt submitted successfully.');
      setForm(initialForm);
      setSuggestions([]);
    } catch (exception) {
      setError(exception.response?.data?.message || 'Could not submit doubt.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <>
      <PageHeader title="Submit Doubt" description="Create a real doubt entry and assign it to an available mentor." />

      <div className="grid grid-cols-[minmax(0,1fr)_360px] gap-6">
        <form onSubmit={handleSubmit} className="rounded-lg border border-slate-200 bg-white p-6 shadow-dashboard">
          <div className="space-y-5">
            <ErrorAlert message={error} />
            {message && <div className="rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">{message}</div>}

            <div>
              <label className="field-label" htmlFor="title">Title</label>
              <input
                id="title"
                className="field-input mt-1"
                value={form.title}
                onChange={(event) => setForm({ ...form, title: event.target.value })}
                required
              />
            </div>

            <div>
              <label className="field-label" htmlFor="category">Category</label>
              <select
                id="category"
                className="field-input mt-1"
                value={form.category}
                onChange={(event) => setForm({ ...form, category: event.target.value })}
              >
                <option value="CONCEPTUAL">Conceptual</option>
                <option value="CODING">Coding</option>
                <option value="DEBUGGING">Debugging</option>
              </select>
            </div>

            <div>
              <label className="field-label" htmlFor="description">Description</label>
              <textarea
                id="description"
                className="field-input mt-1 min-h-44 resize-y"
                value={form.description}
                onChange={(event) => setForm({ ...form, description: event.target.value })}
                required
              />
            </div>

            <button type="submit" className="primary-button" disabled={loading}>
              <Send size={17} />
              {loading ? 'Submitting...' : 'Submit Doubt'}
            </button>
          </div>
        </form>

        <aside className="rounded-lg border border-slate-200 bg-white p-5 shadow-dashboard">
          <div className="mb-4 flex items-center gap-2">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-blue-50 text-primary">
              <Lightbulb size={18} />
            </div>
            <h2 className="text-base font-bold text-slate-950">FAQ Suggestions</h2>
          </div>

          {suggestions.length > 0 ? (
            <div className="space-y-3">
              {suggestions.map((suggestion) => (
                <div key={suggestion.doubtId} className="rounded-lg border border-blue-100 bg-blue-50 p-3">
                  <p className="text-sm font-semibold text-slate-900">{suggestion.title}</p>
                  <p className="mt-1 text-xs font-medium text-primary">{suggestion.category}</p>
                  <p className="mt-2 text-sm text-slate-600">{suggestion.responseText}</p>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-sm text-slate-500">Suggestions will appear when a solved doubt matches your text.</p>
          )}
        </aside>
      </div>
    </>
  );
}
