import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { docaService, type DocaForm } from '../services/docaService';
import { terminalService } from '../services/terminalService';
import type { Doca, StatusDoca } from '../types/api';
import { extractApiError } from '../services/api';
import PageHeader from '../components/PageHeader';
import Modal from '../components/Modal';
import FormField from '../components/FormField';
import StatusBadge from '../components/StatusBadge';
import { useAuth } from '../contexts/AuthContext';

const STATUSES: StatusDoca[] = ['DISPONIVEL', 'OCUPADA', 'MANUTENCAO', 'INTERDITADA'];

export default function Docas() {
  const { hasRole } = useAuth();
  const canEdit = hasRole('ADMINISTRADOR', 'SUPERVISOR');
  const canChangeStatus = hasRole('ADMINISTRADOR', 'SUPERVISOR', 'OPERADOR');
  const isAdmin = hasRole('ADMINISTRADOR');
  const qc = useQueryClient();
  const [filterTerminal, setFilterTerminal] = useState<number | ''>('');
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<Doca | null>(null);
  const [error, setError] = useState<string | null>(null);

  const terminais = useQuery({ queryKey: ['terminais'], queryFn: () => terminalService.list(0, 100) });
  const docas = useQuery({
    queryKey: ['docas', filterTerminal],
    queryFn: () => docaService.list({ terminalId: filterTerminal || undefined, size: 200 }),
  });

  const { register, handleSubmit, reset } = useForm<DocaForm>();

  const openCreate = () => {
    setEditing(null);
    reset({ terminalId: undefined, codigo: '', localizacao: '', status: 'DISPONIVEL' });
    setError(null);
    setOpen(true);
  };
  const openEdit = (d: Doca) => {
    setEditing(d);
    reset({ terminalId: d.terminalId, codigo: d.codigo, localizacao: d.localizacao ?? '', status: d.status });
    setError(null);
    setOpen(true);
  };

  const saveMutation = useMutation({
    mutationFn: (data: DocaForm) =>
      editing ? docaService.update(editing.id, data) : docaService.create(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['docas'] });
      setOpen(false);
    },
    onError: (err) => setError(extractApiError(err)),
  });

  const statusMutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: StatusDoca }) =>
      docaService.updateStatus(id, status),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['docas'] }),
    onError: (err) => alert(extractApiError(err)),
  });

  const removeMutation = useMutation({
    mutationFn: (id: number) => docaService.remove(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['docas'] }),
    onError: (err) => alert(extractApiError(err)),
  });

  return (
    <div>
      <PageHeader
        title="Docas"
        description="Status em tempo real e cadastro de docas."
        actions={canEdit && <button onClick={openCreate} className="btn-primary">Nova doca</button>}
      />

      <div className="card mb-4 flex items-center gap-3">
        <label className="text-sm text-slate-600">Terminal:</label>
        <select
          className="input max-w-xs"
          value={filterTerminal}
          onChange={(e) => setFilterTerminal(e.target.value ? Number(e.target.value) : '')}
        >
          <option value="">Todos</option>
          {terminais.data?.content.map((t) => (
            <option key={t.id} value={t.id}>{t.nome}</option>
          ))}
        </select>
      </div>

      <div className="card overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="text-left text-slate-500 border-b">
            <tr>
              <th className="py-2">Codigo</th>
              <th>Terminal</th>
              <th>Localizacao</th>
              <th>Status</th>
              {canChangeStatus && <th>Alterar status</th>}
              {canEdit && <th></th>}
            </tr>
          </thead>
          <tbody>
            {docas.isLoading && <tr><td colSpan={6} className="py-3 text-slate-400">Carregando...</td></tr>}
            {docas.data?.content.map((d) => (
              <tr key={d.id} className="border-b last:border-0">
                <td className="py-2 font-semibold">{d.codigo}</td>
                <td>{d.terminalNome}</td>
                <td className="text-slate-500">{d.localizacao ?? '-'}</td>
                <td><StatusBadge value={d.status} /></td>
                {canChangeStatus && (
                  <td>
                    <select
                      className="input max-w-xs text-xs"
                      value={d.status}
                      onChange={(e) => statusMutation.mutate({ id: d.id, status: e.target.value as StatusDoca })}
                    >
                      {STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
                    </select>
                  </td>
                )}
                {canEdit && (
                  <td className="text-right space-x-2">
                    <button className="text-brand underline text-xs" onClick={() => openEdit(d)}>Editar</button>
                    {isAdmin && (
                      <button
                        className="text-red-600 underline text-xs"
                        onClick={() => {
                          if (confirm(`Excluir doca ${d.codigo}?`)) removeMutation.mutate(d.id);
                        }}
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
        title={editing ? 'Editar doca' : 'Nova doca'}
        onClose={() => setOpen(false)}
        footer={
          <>
            <button className="btn-secondary" onClick={() => setOpen(false)}>Cancelar</button>
            <button
              className="btn-primary"
              onClick={handleSubmit((d) => saveMutation.mutate({ ...d, terminalId: Number(d.terminalId) }))}
              disabled={saveMutation.isPending}
            >
              {saveMutation.isPending ? 'Salvando...' : 'Salvar'}
            </button>
          </>
        }
      >
        <form>
          <FormField label="Terminal">
            <select className="input" {...register('terminalId', { valueAsNumber: true, required: true })}>
              <option value="">Selecione</option>
              {terminais.data?.content.map((t) => (
                <option key={t.id} value={t.id}>{t.nome}</option>
              ))}
            </select>
          </FormField>
          <FormField label="Codigo">
            <input className="input" {...register('codigo', { required: true })} />
          </FormField>
          <FormField label="Localizacao">
            <input className="input" {...register('localizacao')} />
          </FormField>
          <FormField label="Status">
            <select className="input" {...register('status')}>
              {STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
            </select>
          </FormField>
          {error && (
            <div className="mt-3 p-2 rounded bg-red-50 border border-red-200 text-sm text-red-700">
              {error}
            </div>
          )}
        </form>
      </Modal>
    </div>
  );
}
