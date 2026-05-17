import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { estacionamentoService, type EstacionamentoForm } from '../services/estacionamentoService';
import { terminalService } from '../services/terminalService';
import type { Estacionamento, StatusEstacionamento } from '../types/api';
import { extractApiError } from '../services/api';
import PageHeader from '../components/PageHeader';
import Modal from '../components/Modal';
import FormField from '../components/FormField';
import StatusBadge from '../components/StatusBadge';
import { useAuth } from '../contexts/AuthContext';

const STATUSES: StatusEstacionamento[] = ['ATIVO', 'LOTADO', 'MANUTENCAO', 'INTERDITADO'];

export default function Estacionamentos() {
  const { hasRole } = useAuth();
  const canEdit = hasRole('ADMINISTRADOR', 'SUPERVISOR');
  const isAdmin = hasRole('ADMINISTRADOR');
  const qc = useQueryClient();
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<Estacionamento | null>(null);
  const [error, setError] = useState<string | null>(null);

  const terminais = useQuery({ queryKey: ['terminais'], queryFn: () => terminalService.list(0, 100) });
  const ests = useQuery({
    queryKey: ['estacionamentos'],
    queryFn: () => estacionamentoService.list({ size: 100 }),
  });

  const { register, handleSubmit, reset } = useForm<EstacionamentoForm>();

  const openCreate = () => {
    setEditing(null);
    reset({ terminalId: undefined, nome: '', capacidade: 10, status: 'ATIVO' });
    setError(null);
    setOpen(true);
  };

  const openEdit = (e: Estacionamento) => {
    setEditing(e);
    reset({ terminalId: e.terminalId, nome: e.nome, capacidade: e.capacidade, status: e.status });
    setError(null);
    setOpen(true);
  };

  const saveMutation = useMutation({
    mutationFn: (data: EstacionamentoForm) =>
      editing ? estacionamentoService.update(editing.id, data) : estacionamentoService.create(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['estacionamentos'] });
      setOpen(false);
    },
    onError: (err) => setError(extractApiError(err)),
  });

  const removeMutation = useMutation({
    mutationFn: (id: number) => estacionamentoService.remove(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['estacionamentos'] }),
    onError: (err) => alert(extractApiError(err)),
  });

  return (
    <div>
      <PageHeader
        title="Estacionamentos"
        description="Areas de espera vinculadas a cada terminal."
        actions={canEdit && <button className="btn-primary" onClick={openCreate}>Novo estacionamento</button>}
      />

      <div className="card overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="text-left text-slate-500 border-b">
            <tr>
              <th className="py-2">#</th>
              <th>Terminal</th>
              <th>Nome</th>
              <th>Capacidade</th>
              <th>Status</th>
              {canEdit && <th></th>}
            </tr>
          </thead>
          <tbody>
            {ests.data?.content.map((e) => (
              <tr key={e.id} className="border-b last:border-0">
                <td className="py-2">{e.id}</td>
                <td>{e.terminalNome}</td>
                <td>{e.nome}</td>
                <td>{e.capacidade}</td>
                <td><StatusBadge value={e.status} /></td>
                {canEdit && (
                  <td className="text-right space-x-2">
                    <button className="text-brand underline text-xs" onClick={() => openEdit(e)}>Editar</button>
                    {isAdmin && (
                      <button
                        className="text-red-600 underline text-xs"
                        onClick={() => { if (confirm(`Excluir "${e.nome}"?`)) removeMutation.mutate(e.id); }}
                      >
                        Excluir
                      </button>
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
        title={editing ? 'Editar estacionamento' : 'Novo estacionamento'}
        onClose={() => setOpen(false)}
        footer={
          <>
            <button className="btn-secondary" onClick={() => setOpen(false)}>Cancelar</button>
            <button
              className="btn-primary"
              onClick={handleSubmit((d) => saveMutation.mutate({ ...d, terminalId: Number(d.terminalId), capacidade: Number(d.capacidade) }))}
              disabled={saveMutation.isPending}
            >
              Salvar
            </button>
          </>
        }
      >
        <form>
          <FormField label="Terminal">
            <select className="input" {...register('terminalId', { valueAsNumber: true, required: true })}>
              <option value="">Selecione</option>
              {terminais.data?.content.map((t) => <option key={t.id} value={t.id}>{t.nome}</option>)}
            </select>
          </FormField>
          <FormField label="Nome">
            <input className="input" {...register('nome', { required: true })} />
          </FormField>
          <FormField label="Capacidade">
            <input className="input" type="number" min={1} {...register('capacidade', { valueAsNumber: true, required: true })} />
          </FormField>
          <FormField label="Status">
            <select className="input" {...register('status')}>
              {STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
            </select>
          </FormField>
          {error && (
            <div className="mt-3 p-2 rounded bg-red-50 border border-red-200 text-sm text-red-700">{error}</div>
          )}
        </form>
      </Modal>
    </div>
  );
}
