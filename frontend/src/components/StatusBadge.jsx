export default function StatusBadge({ status }) {
  const styles = {
    OPEN: 'bg-slate-100 text-slate-700',
    ASSIGNED: 'bg-blue-50 text-primary',
    IN_PROGRESS: 'bg-amber-50 text-amber-700',
    RESOLVED: 'bg-emerald-50 text-emerald-700',
  };

  return (
    <span className={`inline-flex min-w-24 justify-center rounded-lg px-2.5 py-1 text-xs font-semibold ${styles[status] || styles.OPEN}`}>
      {status?.replace('_', ' ') || 'OPEN'}
    </span>
  );
}
