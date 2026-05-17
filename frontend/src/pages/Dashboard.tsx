import { useQuery } from '@tanstack/react-query';
import { dashboardService } from '../services/dashboardService';
import MetricCard from '../components/MetricCard';
import StatusBadge from '../components/StatusBadge';
import PageHeader from '../components/PageHeader';

function formatDuration(secs?: number): string {
  if (secs == null) return '-';
  const m = Math.floor(secs / 60);
  const s = Math.floor(secs % 60);
  return `${m}m ${s}s`;
}

export default function Dashboard() {
  const { data, isLoading, isError } = useQuery({
    queryKey: ['dashboard'],
    queryFn: () => dashboardService.resumo(),
    refetchInterval: 15000,
  });

  if (isLoading) return <p className="text-slate-500">Carregando indicadores...</p>;
  if (isError || !data) return <p className="text-red-600">Falha ao carregar o dashboard.</p>;

  const taxaPct = (data.taxaOcupacaoDocas * 100).toFixed(0);

  return (
    <div>
      <PageHeader
        title="Dashboard Operacional"
        description="Visao em tempo real do patio. Atualiza a cada 15 segundos."
      />

      <div className="grid grid-cols-2 md:grid-cols-5 gap-4 mb-6">
        <MetricCard
          label="Docas Ocupadas"
          value={`${data.docasOcupadas}/${data.totalDocas}`}
          hint={`${taxaPct}% de ocupacao`}
          accent="brand"
        />
        <MetricCard
          label="Operacoes Ativas"
          value={data.operacoesEmAndamento}
          hint={`${data.operacoesAgendadas} agendadas`}
          accent="amber"
        />
        <MetricCard
          label="Veiculos no Patio"
          value={data.veiculosNoPatio}
          accent="brand"
        />
        <MetricCard
          label="Tempo Medio Hoje"
          value={formatDuration(data.tempoMedioSegundosHoje ?? undefined)}
          hint={`${data.operacoesFinalizadasHoje} finalizadas hoje`}
          accent="green"
        />
        <MetricCard
          label="Incidentes Abertos"
          value={data.incidentesAbertos}
          accent="red"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="card lg:col-span-2">
          <h2 className="font-semibold text-slate-800 mb-3">Mapa de Docas</h2>
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
            {data.docas.map((doca) => (
              <div
                key={doca.id}
                className="border rounded-md p-3 hover:shadow-sm transition-shadow"
              >
                <div className="flex items-center justify-between">
                  <span className="font-semibold text-slate-800">{doca.codigo}</span>
                  <StatusBadge value={doca.status} />
                </div>
                <p className="text-xs text-slate-500 mt-1">{doca.terminalNome}</p>
                {doca.localizacao && (
                  <p className="text-xs text-slate-400 truncate">{doca.localizacao}</p>
                )}
              </div>
            ))}
          </div>
        </div>

        <div className="card">
          <h2 className="font-semibold text-slate-800 mb-3">Incidentes por Gravidade</h2>
          <ul className="space-y-2">
            {(['CRITICO', 'ALTO', 'MEDIO', 'BAIXO'] as const).map((nivel) => (
              <li key={nivel} className="flex items-center justify-between">
                <StatusBadge value={nivel} />
                <span className="text-sm font-semibold text-slate-700">
                  {data.incidentesAbertosPorGravidade[nivel] ?? 0}
                </span>
              </li>
            ))}
          </ul>
        </div>
      </div>

      <div className="card mt-6">
        <h2 className="font-semibold text-slate-800 mb-3">Operacoes Recentes</h2>
        <table className="w-full text-sm">
          <thead className="text-left text-slate-500 border-b">
            <tr>
              <th className="py-2">#</th>
              <th>Veiculo</th>
              <th>Doca/Vaga</th>
              <th>Tipo</th>
              <th>Status</th>
              <th>Inicio</th>
            </tr>
          </thead>
          <tbody>
            {data.operacoesRecentes.map((op) => (
              <tr key={op.id} className="border-b last:border-0">
                <td className="py-2">{op.id}</td>
                <td>{op.veiculoPlaca ?? '-'}</td>
                <td>{op.docaCodigo ?? op.vagaCodigo ?? '-'}</td>
                <td>{op.tipo}</td>
                <td><StatusBadge value={op.status} /></td>
                <td className="text-xs text-slate-500">
                  {op.iniciadaEm ? new Date(op.iniciadaEm).toLocaleString('pt-BR') : '-'}
                </td>
              </tr>
            ))}
            {data.operacoesRecentes.length === 0 && (
              <tr>
                <td colSpan={6} className="py-4 text-center text-slate-400">
                  Nenhuma operacao registrada.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
