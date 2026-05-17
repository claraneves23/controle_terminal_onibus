interface Props {
  label: string;
  value: string | number;
  hint?: string;
  accent?: 'brand' | 'green' | 'amber' | 'red';
}

const ACCENT: Record<NonNullable<Props['accent']>, string> = {
  brand: 'border-brand text-brand',
  green: 'border-green-600 text-green-700',
  amber: 'border-amber-500 text-amber-700',
  red: 'border-red-600 text-red-700',
};

export default function MetricCard({ label, value, hint, accent = 'brand' }: Props) {
  return (
    <div className={`card border-t-4 ${ACCENT[accent]}`}>
      <p className="text-xs uppercase text-slate-500 font-semibold">{label}</p>
      <p className="text-3xl font-bold mt-1 text-slate-800">{value}</p>
      {hint && <p className="text-xs text-slate-500 mt-1">{hint}</p>}
    </div>
  );
}
