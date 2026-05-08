import { BookOpen, FileText } from 'lucide-react';
import { doubtService } from '../services/doubtService.js';
import StatusBadge from './StatusBadge.jsx';

export default function DoubtTable({ doubts }) {
  function openAttachment(doubtId) {
    doubtService.openAttachment(doubtId).catch(() => {});
  }

  return (
    <div className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-dashboard">
      <table className="w-full text-left text-sm">
        <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
          <tr>
            <th className="px-4 py-3">Title</th>
            <th className="px-4 py-3">Subject</th>
            <th className="px-4 py-3">Category</th>
            <th className="px-4 py-3">Status</th>
            <th className="px-4 py-3">Mentor</th>
            <th className="px-4 py-3">Latest Response</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {doubts.map((doubt) => (
            <tr key={doubt.id} className="align-top">
              <td className="px-4 py-4">
                <p className="font-semibold text-slate-900">{doubt.title}</p>
                <p className="mt-1 line-clamp-2 text-xs text-slate-500">{doubt.description}</p>
                <div className="mt-2 flex flex-wrap gap-2">
                  {doubt.contextNotes && (
                    <span className="inline-flex items-center gap-1 rounded-md bg-slate-100 px-2 py-1 text-xs font-medium text-slate-600">
                      <BookOpen size={12} />
                      Context
                    </span>
                  )}
                  {doubt.hasPdfAttachment && (
                    <button
                      type="button"
                      className="inline-flex items-center gap-1 rounded-md bg-blue-50 px-2 py-1 text-xs font-semibold text-primary"
                      onClick={() => openAttachment(doubt.id)}
                    >
                      <FileText size={12} />
                      PDF
                    </button>
                  )}
                </div>
              </td>
              <td className="px-4 py-4 text-slate-700">{doubt.subject || 'General'}</td>
              <td className="px-4 py-4 text-slate-700">{doubt.category}</td>
              <td className="px-4 py-4">
                <StatusBadge status={doubt.status} />
              </td>
              <td className="px-4 py-4 text-slate-700">{doubt.mentorName || 'Unassigned'}</td>
              <td className="max-w-sm px-4 py-4 text-slate-600">{doubt.latestResponse || 'No response yet'}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
