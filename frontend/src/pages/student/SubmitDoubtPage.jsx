import { FileText, Lightbulb, Mic, MicOff, Send, Sparkles, X } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import ErrorAlert from '../../components/ErrorAlert.jsx';
import PageHeader from '../../components/PageHeader.jsx';
import { getApiErrorMessage } from '../../services/api.js';
import { doubtService } from '../../services/doubtService.js';

const DRAFT_STORAGE_KEY = 'doubtflow_submit_draft';
const MAX_PDF_BYTES = 3 * 1024 * 1024;

const subjects = [
  'Java',
  'Python',
  'Data Structures',
  'DBMS',
  'Operating Systems',
  'Computer Networks',
  'Mathematics',
  'General',
];

const categoryFocus = {
  CONCEPTUAL: 'Explain the concept, key formula or rule, and the exact point where the understanding breaks.',
  CODING: 'Share the goal, language, current approach, expected output, and the smallest code snippet that reproduces the issue.',
  DEBUGGING: 'Include the error message, stack trace, input data, environment, and the last change made before the failure.',
};

function buildPromptTemplate(subject, category) {
  const focus = categoryFocus[category] || categoryFocus.CONCEPTUAL;

  return [
    `Subject: ${subject || 'General'}`,
    `Doubt type: ${category}`,
    `Focus: ${focus}`,
    'Context to include: what I tried, where I am stuck, and what answer format will help me move forward.',
  ].join('\n');
}

function createBlankForm() {
  return {
    title: '',
    description: '',
    category: 'CONCEPTUAL',
    subject: 'Java',
    contextNotes: '',
    promptTemplate: buildPromptTemplate('Java', 'CONCEPTUAL'),
    pdfFileName: '',
    pdfContentType: '',
    pdfData: '',
  };
}

function loadInitialForm() {
  try {
    const savedDraft = JSON.parse(localStorage.getItem(DRAFT_STORAGE_KEY));

    if (savedDraft) {
      return {
        ...createBlankForm(),
        ...savedDraft,
        pdfFileName: '',
        pdfContentType: '',
        pdfData: '',
      };
    }
  } catch {
    localStorage.removeItem(DRAFT_STORAGE_KEY);
  }

  return createBlankForm();
}

export default function SubmitDoubtPage() {
  const [form, setForm] = useState(loadInitialForm);
  const [suggestions, setSuggestions] = useState([]);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [listening, setListening] = useState(false);
  const [speechSupported, setSpeechSupported] = useState(false);
  const recognitionRef = useRef(null);

  useEffect(() => {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    setSpeechSupported(Boolean(SpeechRecognition));

    return () => {
      recognitionRef.current?.stop();
    };
  }, []);

  useEffect(() => {
    const draft = {
      title: form.title,
      description: form.description,
      category: form.category,
      subject: form.subject,
      contextNotes: form.contextNotes,
      promptTemplate: form.promptTemplate,
    };

    localStorage.setItem(DRAFT_STORAGE_KEY, JSON.stringify(draft));
  }, [form.title, form.description, form.category, form.subject, form.contextNotes, form.promptTemplate]);

  useEffect(() => {
    const combinedText = `${form.subject} ${form.title} ${form.description} ${form.contextNotes}`.trim();

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
  }, [form.subject, form.title, form.description, form.contextNotes]);

  function updateField(field, value) {
    setForm((currentForm) => ({ ...currentForm, [field]: value }));
  }

  function updateSubject(value) {
    setForm((currentForm) => ({
      ...currentForm,
      subject: value,
      promptTemplate: buildPromptTemplate(value, currentForm.category),
    }));
  }

  function updateCategory(value) {
    setForm((currentForm) => ({
      ...currentForm,
      category: value,
      promptTemplate: buildPromptTemplate(currentForm.subject, value),
    }));
  }

  function applyPromptTemplate() {
    setForm((currentForm) => ({
      ...currentForm,
      description: `${currentForm.promptTemplate}\n\n${currentForm.description}`.trim(),
    }));
  }

  function toggleVoiceInput() {
    if (listening) {
      recognitionRef.current?.stop();
      setListening(false);
      return;
    }

    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;

    if (!SpeechRecognition) {
      setError('Voice input is not available in this browser.');
      return;
    }

    const recognition = new SpeechRecognition();
    recognition.lang = 'en-US';
    recognition.interimResults = false;
    recognition.continuous = false;

    recognition.onresult = (event) => {
      const transcript = Array.from(event.results)
        .map((result) => result[0]?.transcript || '')
        .join(' ')
        .trim();

      if (transcript) {
        setForm((currentForm) => ({
          ...currentForm,
          description: `${currentForm.description} ${transcript}`.trim(),
        }));
      }
    };

    recognition.onerror = () => {
      setError('Voice input stopped before text was captured.');
    };

    recognition.onend = () => {
      setListening(false);
    };

    recognitionRef.current = recognition;
    setError('');
    setListening(true);
    recognition.start();
  }

  function handlePdfChange(event) {
    const file = event.target.files?.[0];
    event.target.value = '';

    if (!file) {
      return;
    }

    if (file.type !== 'application/pdf' && !file.name.toLowerCase().endsWith('.pdf')) {
      setError('Only PDF files can be attached.');
      return;
    }

    if (file.size > MAX_PDF_BYTES) {
      setError('PDF attachment must be 3 MB or smaller.');
      return;
    }

    const reader = new FileReader();

    reader.onload = () => {
      const dataUrl = String(reader.result || '');
      const pdfData = dataUrl.includes(',') ? dataUrl.split(',')[1] : dataUrl;

      setForm((currentForm) => ({
        ...currentForm,
        pdfFileName: file.name,
        pdfContentType: 'application/pdf',
        pdfData,
      }));
      setError('');
    };

    reader.onerror = () => {
      setError('Could not read the PDF attachment.');
    };

    reader.readAsDataURL(file);
  }

  function clearPdf() {
    setForm((currentForm) => ({
      ...currentForm,
      pdfFileName: '',
      pdfContentType: '',
      pdfData: '',
    }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setMessage('');
    setLoading(true);

    try {
      await doubtService.create(form);
      setMessage('Doubt submitted successfully.');
      setForm(createBlankForm());
      localStorage.removeItem(DRAFT_STORAGE_KEY);
      setSuggestions([]);
    } catch (exception) {
      setError(getApiErrorMessage(exception, 'Could not submit doubt.'));
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

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="field-label" htmlFor="subject">Subject</label>
                <select
                  id="subject"
                  className="field-input mt-1"
                  value={form.subject}
                  onChange={(event) => updateSubject(event.target.value)}
                >
                  {subjects.map((subject) => (
                    <option key={subject} value={subject}>{subject}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="field-label" htmlFor="category">Category</label>
                <select
                  id="category"
                  className="field-input mt-1"
                  value={form.category}
                  onChange={(event) => updateCategory(event.target.value)}
                >
                  <option value="CONCEPTUAL">Conceptual</option>
                  <option value="CODING">Coding</option>
                  <option value="DEBUGGING">Debugging</option>
                </select>
              </div>
            </div>

            <div>
              <label className="field-label" htmlFor="title">Title</label>
              <input
                id="title"
                className="field-input mt-1"
                value={form.title}
                onChange={(event) => updateField('title', event.target.value)}
                required
              />
            </div>

            <div>
              <div className="flex items-center justify-between gap-3">
                <label className="field-label" htmlFor="description">Description</label>
                <button
                  type="button"
                  className="secondary-button h-9 w-9 px-0"
                  title={listening ? 'Stop voice input' : 'Start voice input'}
                  onClick={toggleVoiceInput}
                  disabled={!speechSupported}
                >
                  {listening ? <MicOff size={16} /> : <Mic size={16} />}
                </button>
              </div>
              <textarea
                id="description"
                className="field-input mt-1 min-h-44 resize-y"
                value={form.description}
                onChange={(event) => updateField('description', event.target.value)}
                required
              />
            </div>

            <div>
              <label className="field-label" htmlFor="contextNotes">Context Memory</label>
              <textarea
                id="contextNotes"
                className="field-input mt-1 min-h-24 resize-y"
                value={form.contextNotes}
                onChange={(event) => updateField('contextNotes', event.target.value)}
              />
            </div>

            <div>
              <label className="field-label" htmlFor="pdfAttachment">PDF Attachment</label>
              <div className="mt-1 flex items-center gap-3">
                <input
                  id="pdfAttachment"
                  className="field-input"
                  type="file"
                  accept="application/pdf,.pdf"
                  onChange={handlePdfChange}
                />
                {form.pdfFileName && (
                  <button type="button" className="secondary-button h-[42px] w-10 shrink-0 px-0" title="Remove PDF" onClick={clearPdf}>
                    <X size={16} />
                  </button>
                )}
              </div>
              {form.pdfFileName && (
                <div className="mt-2 flex items-center gap-2 text-sm font-medium text-slate-600">
                  <FileText size={15} />
                  <span className="truncate">{form.pdfFileName}</span>
                </div>
              )}
            </div>

            <button type="submit" className="primary-button" disabled={loading}>
              <Send size={17} />
              {loading ? 'Submitting...' : 'Submit Doubt'}
            </button>
          </div>
        </form>

        <aside className="space-y-4">
          <section className="rounded-lg border border-slate-200 bg-white p-5 shadow-dashboard">
            <div className="mb-4 flex items-center gap-2">
              <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-blue-50 text-primary">
                <Sparkles size={18} />
              </div>
              <h2 className="text-base font-bold text-slate-950">Prompt Template</h2>
            </div>

            <textarea
              className="field-input min-h-44 resize-y"
              value={form.promptTemplate}
              onChange={(event) => updateField('promptTemplate', event.target.value)}
            />

            <button type="button" className="secondary-button mt-3 w-full" onClick={applyPromptTemplate}>
              <Sparkles size={16} />
              Use Template
            </button>
          </section>

          <section className="rounded-lg border border-slate-200 bg-white p-5 shadow-dashboard">
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
          </section>
        </aside>
      </div>
    </>
  );
}
