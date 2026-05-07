import StatusBadge from './StatusBadge.jsx';

export default function DoubtTable({ doubts }) {
  return (
    <div className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-dashboard">
      <table className="w-full text-left text-sm">
        <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
          <tr>
            <th className="px-4 py-3">Title</th>
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
              </td>
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
