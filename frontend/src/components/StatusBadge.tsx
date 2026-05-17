const COLORS: Record<string, string> = {
  // Doca
  DISPONIVEL: 'bg-green-100 text-green-800',
  OCUPADA: 'bg-amber-100 text-amber-800',
  MANUTENCAO: 'bg-slate-200 text-slate-700',
  INTERDITADA: 'bg-red-100 text-red-800',
  INTERDITADO: 'bg-red-100 text-red-800',
  // Vaga
  LIVRE: 'bg-green-100 text-green-800',
  RESERVADA: 'bg-blue-100 text-blue-800',
  // Estacionamento
  ATIVO: 'bg-green-100 text-green-800',
  LOTADO: 'bg-amber-100 text-amber-800',
  // Operacao
  AGENDADA: 'bg-blue-100 text-blue-800',
  EM_ANDAMENTO: 'bg-amber-100 text-amber-800',
  FINALIZADA: 'bg-green-100 text-green-800',
  CANCELADA: 'bg-slate-200 text-slate-700',
  // Incidente
  ABERTO: 'bg-red-100 text-red-800',
  EM_ANALISE: 'bg-amber-100 text-amber-800',
  RESOLVIDO: 'bg-green-100 text-green-800',
  // Gravidade
  BAIXO: 'bg-slate-200 text-slate-700',
  MEDIO: 'bg-amber-100 text-amber-800',
  ALTO: 'bg-orange-100 text-orange-800',
  CRITICO: 'bg-red-100 text-red-800',
};

export default function StatusBadge({ value }: { value: string | undefined | null }) {
  if (!value) return <span className="text-slate-400">-</span>;
  const cls = COLORS[value] ?? 'bg-slate-100 text-slate-700';
  return (
    <span className={`inline-block px-2 py-0.5 rounded text-xs font-semibold ${cls}`}>
      {value.replace(/_/g, ' ')}
    </span>
  );
}
