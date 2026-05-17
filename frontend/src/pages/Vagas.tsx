import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { vagaService, type VagaForm } from '../services/vagaService';
import { estacionamentoService } from '../services/estacionamentoService';
import type { StatusVaga, Vaga } from '../types/api';
import { extractApiError } from '../services/api';
import PageHeader from '../components/PageHeader';
import Modal from '../components/Modal';
import FormField from '../components/FormField';
import StatusBadge from '../components/StatusBadge';
import { useAuth } from '../contexts/AuthContext';

const STATUSES: StatusVaga[] = ['LIVRE', 'OCUPADA', 'RESERVADA', 'INTERDITADA'];

export default function Vagas() {
  const { hasRole } = useAuth();
  const canEdit = hasRole('ADMINISTRADOR', 'SUPERVISOR');
  const canChangeStatus = hasRole('ADMINISTRADOR', 'SUPERVISOR', 'OPERADOR');
  const isAdmin = hasRole('ADMINISTRADOR');
  const qc = useQueryClient();
  const [filterEst, setFilterEst] = useState<number | ''>('');
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<Vaga | null>(null);
  const [error, setError] = useState<string | null>(null);

  const ests = useQuery({
    queryKey: ['estacionamentos'],
    queryFn: () => estacionamentoService.list({ size: 100 }),
  });
  const vagas = useQuery({
    queryKey: ['vagas', filterEst],
    queryFn: () => vagaService.list({ estacionamentoId: filterEst || undefined, size: 200 }),
  });

  const { register, handleSubmit, reset } = useForm<VagaForm>();

  const openCreate = () => {
    setEditing(null);
    reset({ estacionamentoId: undefined, codigo: '', status: 'LIVRE' });
    setError(null);
    setOpen(true);
  };
  const openEdit = (v: Vaga) => {
    setEditing(v);
    reset({ estacionamentoId: v.estacionamentoId, codigo: v.codigo, status: v.status });
    setError(null);
    setOpen(true);
  };

  const saveMutation = useMutation({
    mutationFn: (d: VagaForm) =>
      editing ? vagaService.update(editing.id, d) : vagaService.create(d),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['vagas'] }); setOpen(false); },
    onError: (err) => setError(extractApiError(err)),
  });

  const statusMutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: StatusVaga }) =>
      vagaService.updateStatus(id, status),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['vagas'] }),
    onError: (err) => alert(extractApiError(err)),
  });

  const removeMutation = useMutation({
    mutationFn: (id: number) => vagaService.remove(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['vagas'] }),
    onError: (err) => alert(extractApiError(err)),
  });

  return (
    <div>
      <PageHeader
        title="Vagas"
        description="Vagas do patio agrupadas por estacionamento."
        actions={canEdit && <button className="btn-primary" onClick={openCreate}>Nova vaga</button>}
      />

      <div className="card mb-4 flex items-center gap-3">
        <label className="text-sm text-slate-600">Estacionamento:</label>
        <select
          className="input max-w-xs"
          value={filterEst}
          onChange={(e) => setFilterEst(e.target.value ? Number(e.target.value) : '')}
        >
          <option value="">Todos</option>
          {ests.data?.content.map((e) => <option key={e.id} value={e.id}>{e.nome}</option>)}
        </select>
      </div>

      <div className="card overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="text-left text-slate-500 border-b">
            <tr>
              <th className="py-2">Codigo</th>
              <th>Estacionamento</th>
              <th>Status</th>
              {canChangeStatus && <th>Alterar status</th>}
              {canEdit && <th></th>}
            </tr>
          </thead>
          <tbody>
            {vagas.data?.content.map((v) => (
              <tr key={v.id} className="border-b last:border-0">
                <td className="py-2 font-semibold">{v.codigo}</td>
                <td>{v.estacionamentoNome}</td>
                <td><StatusBadge value={v.status} /></td>
                {canChangeStatus && (
                  <td>
                    <select
                      className="input max-w-xs text-xs"
                      value={v.status}
                      onChange={(e) => statusMutation.mutate({ id: v.id, status: e.target.value as StatusVaga })}
                    >
                      {STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
                    </select>
                  </td>
                )}
                {canEdit && (
                  <td className="text-right space-x-2">
                    <button className="text-brand underline text-xs" onClick={() => openEdit(v)}>Editar</button>
                    {isAdmin && (
                      <button
                        className="text-red-600 underline text-xs"
                        onClick={() => { if (confirm(`Excluir vaga ${v.codigo}?`)) removeMutation.mutate(v.id); }}
                      >Excluir</button>
                    )}
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <Modal
        open={open}
        title={editing ? 'Editar vaga' : 'Nova vaga'}
        onClose={() => setOpen(false)}
        footer={
          <>
            <button className="btn-secondary" onClick={() => setOpen(false)}>Cancelar</button>
            <button
              className="btn-primary"
              onClick={handleSubmit((d) => saveMutation.mutate({ ...d, estacionamentoId: Number(d.estacionamentoId) }))}
              disabled={saveMutation.isPending}
            >Salvar</button>
          </>
        }
      >
        <form>
          <FormField label="Estacionamento">
            <select className="input" {...register('estacionamentoId', { valueAsNumber: true, required: true })}>
              <option value="">Selecione</option>
              {ests.data?.content.map((e) => <option key={e.id} value={e.id}>{e.nome}</option>)}
            </select>
          </FormField>
          <FormField label="Codigo">
            <input className="input" {...register('codigo', { required: true })} />
          </FormField>
          <FormField label="Status">
            <select className="input" {...register('status')}>
              {STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
            </select>
          </FormField>
          {error && <div className="mt-3 p-2 rounded bg-red-50 border border-red-200 text-sm text-red-700">{error}</div>}
        </form>
      </Modal>
    </div>
  );
}
