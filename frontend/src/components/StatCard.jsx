export default function StatCard({ label, value, icon: Icon, tone = 'blue' }) {
  const tones = {
    blue: 'bg-blue-50 text-primary',
    green: 'bg-emerald-50 text-emerald-600',
    amber: 'bg-amber-50 text-amber-600',
    slate: 'bg-slate-100 text-slate-700',
  };

  return (
    <div className="rounded-lg border border-slate-200 bg-white p-5 shadow-dashboard">
      <div className="flex items-center justify-between gap-4">
        <div>
          <p className="text-sm font-medium text-slate-500">{label}</p>
          <p className="mt-2 text-3xl font-bold text-slate-950">{value}</p>
        </div>
        {Icon && (
          <div className={`flex h-11 w-11 items-center justify-center rounded-lg ${tones[tone]}`}>
            <Icon size={22} />
          </div>
        )}
      </div>
    </div>
  );
}
