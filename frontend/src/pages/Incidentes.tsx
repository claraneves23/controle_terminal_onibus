import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { incidenteService, type IncidenteForm } from '../services/incidenteService';
import { tipoIncidenteService } from '../services/tipoIncidenteService';
import { terminalService } from '../services/terminalService';
import { docaService } from '../services/docaService';
import { vagaService } from '../services/vagaService';
import { operacaoService } from '../services/operacaoService';
import type { Incidente, NivelGravidade, StatusIncidente } from '../types/api';
import { extractApiError } from '../services/api';
import PageHeader from '../components/PageHeader';
import Modal from '../components/Modal';
import FormField from '../components/FormField';
import StatusBadge from '../components/StatusBadge';
import { useAuth } from '../contexts/AuthContext';

const STATUSES: StatusIncidente[] = ['ABERTO', 'EM_ANALISE', 'RESOLVIDO', 'CANCELADO'];
const NIVEIS: NivelGravidade[] = ['BAIXO', 'MEDIO', 'ALTO', 'CRITICO'];

export default function Incidentes() {
  const { hasRole } = useAuth();
  const canResolve = hasRole('ADMINISTRADOR', 'SUPERVISOR');
  const isAdmin = hasRole('ADMINISTRADOR');
  const qc = useQueryClient();

  const [filtroStatus, setFiltroStatus] = useState<StatusIncidente | ''>('');
  const [filtroGravidade, setFiltroGravidade] = useState<NivelGravidade | ''>('');
  const [filtroTerminal, setFiltroTerminal] = useState<number | ''>('');

  const [openCreate, setOpenCreate] = useState(false);
  const [openEncerrar, setOpenEncerrar] = useState<Incidente | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [acaoTomada, setAcaoTomada] = useState('');

  const tipos = useQuery({ queryKey: ['tipos-incidente'], queryFn: () => tipoIncidenteService.list({ size: 100 }) });
  const terminais = useQuery({ queryKey: ['terminais'], queryFn: () => terminalService.list(0, 100) });
  const docas = useQuery({ queryKey: ['docas-all'], queryFn: () => docaService.list({ size: 200 }) });
  const vagas = useQuery({ queryKey: ['vagas-all'], queryFn: () => vagaService.list({ size: 200 }) });
  const operacoes = useQuery({ queryKey: ['operacoes-all'], queryFn: () => operacaoService.list({ size: 100 }) });

  const incidentes = useQuery({
    queryKey: ['incidentes', filtroStatus, filtroGravidade, filtroTerminal],
    queryFn: () => incidenteService.list({
      status: filtroStatus || undefined,
      gravidade: filtroGravidade || undefined,
      terminalId: filtroTerminal || undefined,
      size: 100,
    }),
  });

  const { register, handleSubmit, reset } = useForm<IncidenteForm>();

  const onOpenCreate = () => {
    reset({
      tipoIncidenteId: undefined as unknown as number,
      terminalId: undefined as unknown as number,
      ocorridoEm: new Date().toISOString().slice(0, 16),
      descricao: '',
    });
    setError(null);
    setOpenCreate(true);
  };

  const createMutation = useMutation({
    mutationFn: (d: IncidenteForm) => incidenteService.create({
      ...d,
      tipoIncidenteId: Number(d.tipoIncidenteId),
      terminalId: Number(d.terminalId),
      docaId: d.docaId ? Number(d.docaId) : undefined,
      vagaId: d.vagaId ? Number(d.vagaId) : undefined,
      operacaoId: d.operacaoId ? Number(d.operacaoId) : undefined,
    }),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['incidentes'] }); setOpenCreate(false); },
    onError: (err) => setError(extractApiError(err)),
  });

  const encerrarMutation = useMutation({
    mutationFn: ({ id, acao }: { id: number; acao: string }) => incidenteService.encerrar(id, acao),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['incidentes'] }); setOpenEncerrar(null); setAcaoTomada(''); },
    onError: (err) => alert(extractApiError(err)),
  });

  const removeMutation = useMutation({
    mutationFn: (id: number) => incidenteService.remove(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['incidentes'] }),
    onError: (err) => alert(extractApiError(err)),
  });

  return (
    <div>
      <PageHeader
        title="Incidentes"
        description="Registro de ocorrencias operacionais e de seguranca."
        actions={<button className="btn-primary" onClick={onOpenCreate}>Novo incidente</button>}
      />

      <div className="card mb-4 flex flex-wrap gap-3 items-end">
        <div>
          <label className="label">Status</label>
          <select className="input" value={filtroStatus} onChange={(e) => setFiltroStatus(e.target.value as StatusIncidente | '')}>
            <option value="">Todos</option>
            {STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
          </select>
        </div>
        <div>
          <label className="label">Gravidade</label>
          <select className="input" value={filtroGravidade} onChange={(e) => setFiltroGravidade(e.target.value as NivelGravidade | '')}>
            <option value="">Todas</option>
            {NIVEIS.map((n) => <option key={n} value={n}>{n}</option>)}
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
              <th>Tipo</th>
              <th>Gravidade</th>
              <th>Terminal</th>
              <th>Ocorrido em</th>
              <th>Status</th>
              <th>Registrado por</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {incidentes.isLoading && <tr><td colSpan={8} className="py-3 text-slate-400">Carregando...</td></tr>}
            {incidentes.data?.content.map((i) => (
              <tr key={i.id} className="border-b last:border-0">
                <td className="py-2 font-mono text-xs">{i.id}</td>
                <td className="font-semibold">{i.tipoIncidenteNome}</td>
                <td><StatusBadge value={i.nivelGravidade} /></td>
                <td>{i.terminalNome}</td>
                <td className="text-xs text-slate-500">{new Date(i.ocorridoEm).toLocaleString('pt-BR')}</td>
                <td><StatusBadge value={i.status} /></td>
                <td className="text-xs text-slate-500">{i.usuarioRegistroNome ?? '-'}</td>
                <td className="text-right space-x-2">
                  {canResolve && i.status !== 'RESOLVIDO' && i.status !== 'CANCELADO' && (
                    <button
                      className="text-brand underline text-xs"
                      onClick={() => { setOpenEncerrar(i); setAcaoTomada(''); }}
                    >Encerrar</button>
                  )}
                  {isAdmin && (
                    <button
                      className="text-red-600 underline text-xs"
                      onClick={() => { if (confirm(`Excluir incidente #${i.id}?`)) removeMutation.mutate(i.id); }}
                    >Excluir</button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <Modal
        open={openCreate}
        title="Registrar incidente"
        onClose={() => setOpenCreate(false)}
        footer={
          <>
            <button className="btn-secondary" onClick={() => setOpenCreate(false)}>Cancelar</button>
            <button
              className="btn-primary"
              onClick={handleSubmit((d) => createMutation.mutate(d))}
              disabled={createMutation.isPending}
            >Salvar</button>
          </>
        }
      >
        <form>
          <FormField label="Tipo de incidente">
            <select className="input" {...register('tipoIncidenteId', { required: true, valueAsNumber: true })}>
              <option value="">Selecione</option>
              {tipos.data?.content.map((t) => <option key={t.id} value={t.id}>{t.nome} ({t.nivelGravidade})</option>)}
            </select>
          </FormField>
          <FormField label="Terminal">
            <select className="input" {...register('terminalId', { required: true, valueAsNumber: true })}>
              <option value="">Selecione</option>
              {terminais.data?.content.map((t) => <option key={t.id} value={t.id}>{t.nome}</option>)}
            </select>
          </FormField>
          <FormField label="Ocorrido em">
            <input className="input" type="datetime-local" {...register('ocorridoEm', { required: true })} />
          </FormField>
          <FormField label="Descricao">
            <textarea className="input" rows={3} {...register('descricao', { required: true })} />
          </FormField>
          <div className="grid grid-cols-2 gap-3">
            <FormField label="Doca (opcional)">
              <select className="input" {...register('docaId')}>
                <option value="">Nenhuma</option>
                {docas.data?.content.map((d) => <option key={d.id} value={d.id}>{d.codigo}</option>)}
              </select>
            </FormField>
            <FormField label="Vaga (opcional)">
              <select className="input" {...register('vagaId')}>
                <option value="">Nenhuma</option>
                {vagas.data?.content.map((v) => <option key={v.id} value={v.id}>{v.codigo}</option>)}
              </select>
            </FormField>
          </div>
          <FormField label="Operacao (opcional)">
            <select className="input" {...register('operacaoId')}>
              <option value="">Nenhuma</option>
              {operacoes.data?.content.map((o) => (
                <option key={o.id} value={o.id}>#{o.id} - {o.veiculoPlaca ?? '-'} ({o.status})</option>
              ))}
            </select>
          </FormField>
          <FormField label="Acao tomada (opcional)">
            <textarea className="input" rows={2} {...register('acaoTomada')} />
          </FormField>
          {error && <div className="mt-3 p-2 rounded bg-red-50 border border-red-200 text-sm text-red-700">{error}</div>}
        </form>
      </Modal>

      <Modal
        open={!!openEncerrar}
        title={`Encerrar incidente #${openEncerrar?.id ?? ''}`}
        onClose={() => setOpenEncerrar(null)}
        footer={
          <>
            <button className="btn-secondary" onClick={() => setOpenEncerrar(null)}>Cancelar</button>
            <button
              className="btn-primary"
              disabled={!acaoTomada.trim() || encerrarMutation.isPending}
              onClick={() => openEncerrar && encerrarMutation.mutate({ id: openEncerrar.id, acao: acaoTomada })}
            >Confirmar encerramento</button>
          </>
        }
      >
        <p className="text-sm text-slate-600 mb-3">Descreva a acao tomada para resolver o incidente:</p>
        <textarea
          className="input"
          rows={4}
          value={acaoTomada}
          onChange={(e) => setAcaoTomada(e.target.value)}
          placeholder="Acao tomada..."
        />
      </Modal>
    </div>
  );
}
