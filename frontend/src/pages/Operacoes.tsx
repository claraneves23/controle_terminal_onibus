import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { operacaoService, type CheckinForm } from '../services/operacaoService';
import { terminalService } from '../services/terminalService';
import { veiculoService } from '../services/veiculoService';
import { docaService } from '../services/docaService';
import { vagaService } from '../services/vagaService';
import type { StatusOperacao, TipoOperacao } from '../types/api';
import { extractApiError } from '../services/api';
import PageHeader from '../components/PageHeader';
import Modal from '../components/Modal';
import FormField from '../components/FormField';
import StatusBadge from '../components/StatusBadge';
import { useAuth } from '../contexts/AuthContext';

const STATUSES: StatusOperacao[] = ['AGENDADA', 'EM_ANDAMENTO', 'FINALIZADA', 'CANCELADA'];
const TIPOS: TipoOperacao[] = ['CARGA', 'DESCARGA'];

export default function Operacoes() {
  const { hasRole } = useAuth();
  const canCheckin = hasRole('OPERADOR', 'SEGURANCA', 'ADMINISTRADOR');
  const canIniciar = hasRole('OPERADOR', 'SUPERVISOR', 'ADMINISTRADOR');
  const canFinalizar = canIniciar;
  const canCheckout = hasRole('OPERADOR', 'SEGURANCA', 'ADMINISTRADOR');
  const canCancelar = hasRole('SUPERVISOR', 'ADMINISTRADOR');

  const qc = useQueryClient();
  const [filtroStatus, setFiltroStatus] = useState<StatusOperacao | ''>('');
  const [filtroTerminal, setFiltroTerminal] = useState<number | ''>('');
  const [open, setOpen] = useState(false);
  const [step, setStep] = useState(1);
  const [error, setError] = useState<string | null>(null);
  const [recurso, setRecurso] = useState<'DOCA' | 'VAGA'>('DOCA');

  const terminais = useQuery({ queryKey: ['terminais'], queryFn: () => terminalService.list(0, 100) });
  const veiculos = useQuery({ queryKey: ['veiculos-all'], queryFn: () => veiculoService.list({ size: 200 }) });
  const docas = useQuery({ queryKey: ['docas-all'], queryFn: () => docaService.list({ size: 200 }) });
  const vagas = useQuery({ queryKey: ['vagas-all'], queryFn: () => vagaService.list({ size: 200 }) });

  const operacoes = useQuery({
    queryKey: ['operacoes', filtroStatus, filtroTerminal],
    queryFn: () => operacaoService.list({
      status: filtroStatus || undefined,
      terminalId: filtroTerminal || undefined,
      size: 50,
    }),
    refetchInterval: 15000,
  });

  const { register, handleSubmit, reset, watch } = useForm<CheckinForm>();
  const tipoSelecionado = watch('tipo');

  const onOpenCheckin = () => {
    reset({ tipo: 'CARGA', terminalId: undefined as unknown as number, veiculoId: undefined as unknown as number });
    setStep(1);
    setRecurso('DOCA');
    setError(null);
    setOpen(true);
  };

  const checkinMutation = useMutation({
    mutationFn: (d: CheckinForm) => operacaoService.checkin({
      ...d,
      terminalId: Number(d.terminalId),
      veiculoId: Number(d.veiculoId),
      docaId: recurso === 'DOCA' && d.docaId ? Number(d.docaId) : undefined,
      vagaId: recurso === 'VAGA' && d.vagaId ? Number(d.vagaId) : undefined,
      quantidadeVolume: d.quantidadeVolume ? Number(d.quantidadeVolume) : undefined,
      pesoEstimado: d.pesoEstimado ? Number(d.pesoEstimado) : undefined,
    }),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['operacoes'] }); setOpen(false); },
    onError: (err) => setError(extractApiError(err)),
  });

  const actionMutation = useMutation({
    mutationFn: ({ id, action }: { id: number; action: 'iniciar' | 'finalizar' | 'checkout' | 'cancelar' }) => {
      if (action === 'iniciar') return operacaoService.iniciar(id);
      if (action === 'finalizar') return operacaoService.finalizar(id);
      if (action === 'checkout') return operacaoService.checkout(id);
      return operacaoService.cancelar(id);
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: ['operacoes'] }),
    onError: (err) => alert(extractApiError(err)),
  });

  return (
    <div>
      <PageHeader
        title="Operacoes"
        description="Fluxo de check-in, execucao e check-out de cargas e descargas."
        actions={canCheckin && <button className="btn-primary" onClick={onOpenCheckin}>Novo check-in</button>}
      />

      <div className="card mb-4 flex flex-wrap gap-3 items-end">
        <div>
          <label className="label">Status</label>
          <select className="input" value={filtroStatus} onChange={(e) => setFiltroStatus(e.target.value as StatusOperacao | '')}>
            <option value="">Todos</option>
            {STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
          </select>
        </div>
        <div>
          <label className="label">Terminal</label>
          <select className="input" value={filtroTerminal} onChange={(e) => setFiltroTerminal(e.target.value ? Number(e.target.value) : '')}>
            <option value="">Todos</option>
            {terminais.data?.content.map((t) => <option key={t.id} value={t.id}>{t.nome}</option>)}
          </select>
        </div>
      </div>

      <div className="card overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="text-left text-slate-500 border-b">
            <tr>
              <th className="py-2">#</th>
              <th>Terminal</th>
              <th>Veiculo</th>
              <th>Tipo</th>
              <th>Doca/Vaga</th>
              <th>Status</th>
              <th>Agendada</th>
              <th>Acoes</th>
            </tr>
          </thead>
          <tbody>
            {operacoes.isLoading && <tr><td colSpan={8} className="py-3 text-slate-400">Carregando...</td></tr>}
            {operacoes.data?.content.map((o) => (
              <tr key={o.id} className="border-b last:border-0">
                <td className="py-2">
                  <Link className="text-brand underline font-mono text-xs" to={`/operacoes/${o.id}`}>#{o.id}</Link>
                </td>
                <td>{o.terminalNome}</td>
                <td className="font-semibold">{o.veiculoPlaca ?? '-'}</td>
                <td>{o.tipo}</td>
                <td className="text-xs text-slate-500">{o.docaCodigo ?? o.vagaCodigo ?? '-'}</td>
                <td><StatusBadge value={o.status} /></td>
                <td className="text-xs text-slate-500">{o.agendadaEm ? new Date(o.agendadaEm).toLocaleString('pt-BR') : '-'}</td>
                <td className="space-x-2">
                  {o.status === 'AGENDADA' && canIniciar && (
                    <button className="text-brand underline text-xs"
                      onClick={() => actionMutation.mutate({ id: o.id, action: 'iniciar' })}>Iniciar</button>
                  )}
                  {o.status === 'EM_ANDAMENTO' && canFinalizar && (
                    <button className="text-brand underline text-xs"
                      onClick={() => actionMutation.mutate({ id: o.id, action: 'finalizar' })}>Finalizar</button>
                  )}
                  {o.status === 'FINALIZADA' && canCheckout && (
                    <button className="text-green-700 underline text-xs"
                      onClick={() => actionMutation.mutate({ id: o.id, action: 'checkout' })}>Checkout</button>
                  )}
                  {o.status !== 'FINALIZADA' && o.status !== 'CANCELADA' && canCancelar && (
                    <button className="text-red-600 underline text-xs"
                      onClick={() => { if (confirm(`Cancelar operacao #${o.id}?`)) actionMutation.mutate({ id: o.id, action: 'cancelar' }); }}>Cancelar</button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <Modal
        open={open}
        title={`Check-in (passo ${step} de 2)`}
        onClose={() => setOpen(false)}
        footer={
          <>
            <button className="btn-secondary" onClick={() => setOpen(false)}>Cancelar</button>
            {step === 1 && (
              <button className="btn-primary" onClick={() => setStep(2)}>Proximo</button>
            )}
            {step === 2 && (
              <>
                <button className="btn-secondary" onClick={() => setStep(1)}>Voltar</button>
                <button
                  className="btn-primary"
                  onClick={handleSubmit((d) => checkinMutation.mutate(d))}
                  disabled={checkinMutation.isPending}
                >Confirmar check-in</button>
              </>
            )}
          </>
        }
      >
        <form>
          {step === 1 && (
            <>
              <FormField label="Terminal">
                <select className="input" {...register('terminalId', { required: true, valueAsNumber: true })}>
                  <option value="">Selecione</option>
                  {terminais.data?.content.map((t) => <option key={t.id} value={t.id}>{t.nome}</option>)}
                </select>
              </FormField>
              <FormField label="Veiculo">
                <select className="input" {...register('veiculoId', { required: true, valueAsNumber: true })}>
                  <option value="">Selecione</option>
                  {veiculos.data?.content.map((v) => <option key={v.id} value={v.id}>{v.placa} - {v.tipo}</option>)}
                </select>
              </FormField>
              <FormField label="Tipo de operacao">
                <select className="input" {...register('tipo', { required: true })}>
                  {TIPOS.map((t) => <option key={t} value={t}>{t}</option>)}
                </select>
              </FormField>
              <p className="text-xs text-slate-500 mt-2">Tipo selecionado: <strong>{tipoSelecionado ?? 'CARGA'}</strong></p>
            </>
          )}

          {step === 2 && (
            <>
              <div className="mb-3">
                <label className="label">Recurso a alocar</label>
                <div className="flex gap-3 text-sm">
                  <label className="flex items-center gap-1">
                    <input type="radio" checked={recurso === 'DOCA'} onChange={() => setRecurso('DOCA')} /> Doca
                  </label>
                  <label className="flex items-center gap-1">
                    <input type="radio" checked={recurso === 'VAGA'} onChange={() => setRecurso('VAGA')} /> Vaga (patio)
                  </label>
                </div>
              </div>
              {recurso === 'DOCA' && (
                <FormField label="Doca disponivel">
                  <select className="input" {...register('docaId')}>
                    <option value="">Selecione</option>
                    {docas.data?.content
                      .filter((d) => d.status === 'DISPONIVEL')
                      .map((d) => <option key={d.id} value={d.id}>{d.codigo} ({d.terminalNome})</option>)}
                  </select>
                </FormField>
              )}
              {recurso === 'VAGA' && (
                <FormField label="Vaga livre">
                  <select className="input" {...register('vagaId')}>
                    <option value="">Selecione</option>
                    {vagas.data?.content
                      .filter((v) => v.status === 'LIVRE')
                      .map((v) => <option key={v.id} value={v.id}>{v.codigo} ({v.estacionamentoNome})</option>)}
                  </select>
                </FormField>
              )}
              <FormField label="Descricao da carga">
                <input className="input" {...register('descricaoCarga')} />
              </FormField>
              <div className="grid grid-cols-2 gap-3">
                <FormField label="Volume (un)">
                  <input className="input" type="number" min={0} {...register('quantidadeVolume')} />
                </FormField>
                <FormField label="Peso (kg)">
                  <input className="input" type="number" step="0.01" min={0} {...register('pesoEstimado')} />
                </FormField>
              </div>
              <FormField label="Observacao">
                <textarea className="input" rows={2} {...register('observacao')} />
              </FormField>
              {error && <div className="mt-3 p-2 rounded bg-red-50 border border-red-200 text-sm text-red-700">{error}</div>}
            </>
          )}
        </form>
      </Modal>
    </div>
  );
}
