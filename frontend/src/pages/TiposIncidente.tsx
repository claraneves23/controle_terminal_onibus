import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { tipoIncidenteService, type TipoIncidenteForm } from '../services/tipoIncidenteService';
import type { NivelGravidade, TipoIncidente } from '../types/api';
import { extractApiError } from '../services/api';
import PageHeader from '../components/PageHeader';
import Modal from '../components/Modal';
import FormField from '../components/FormField';
import StatusBadge from '../components/StatusBadge';

const NIVEIS: NivelGravidade[] = ['BAIXO', 'MEDIO', 'ALTO', 'CRITICO'];

export default function TiposIncidente() {
  const qc = useQueryClient();
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<TipoIncidente | null>(null);
  const [error, setError] = useState<string | null>(null);

  const tipos = useQuery({
    queryKey: ['tipos-incidente'],
    queryFn: () => tipoIncidenteService.list({ size: 100 }),
  });

  const { register, handleSubmit, reset } = useForm<TipoIncidenteForm>();

  const openCreate = () => {
    setEditing(null);
    reset({ nome: '', descricao: '', nivelGravidade: 'MEDIO' });
    setError(null);
    setOpen(true);
  };

  const openEdit = (t: TipoIncidente) => {
    setEditing(t);
    reset({ nome: t.nome, descricao: t.descricao ?? '', nivelGravidade: t.nivelGravidade });
    setError(null);
    setOpen(true);
  };

  const saveMutation = useMutation({
    mutationFn: (d: TipoIncidenteForm) =>
      editing ? tipoIncidenteService.update(editing.id, d) : tipoIncidenteService.create(d),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['tipos-incidente'] }); setOpen(false); },
    onError: (err) => setError(extractApiError(err)),
  });

  const removeMutation = useMutation({
    mutationFn: (id: number) => tipoIncidenteService.remove(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['tipos-incidente'] }),
    onError: (err) => alert(extractApiError(err)),
  });

  return (
    <div>
      <PageHeader
        title="Tipos de Incidente"
        description="Catalogo de categorias usadas para classificar incidentes."
        actions={<button className="btn-primary" onClick={openCreate}>Novo tipo</button>}
      />

      <div className="card overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="text-left text-slate-500 border-b">
            <tr>
              <th className="py-2">Nome</th>
              <th>Descricao</th>
              <th>Gravidade</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {tipos.isLoading && <tr><td colSpan={4} className="py-3 text-slate-400">Carregando...</td></tr>}
            {tipos.data?.content.map((t) => (
              <tr key={t.id} className="border-b last:border-0">
                <td className="py-2 font-semibold">{t.nome}</td>
                <td className="text-slate-500">{t.descricao ?? '-'}</td>
                <td><StatusBadge value={t.nivelGravidade} /></td>
                <td className="text-right space-x-2">
                  <button className="text-brand underline text-xs" onClick={() => openEdit(t)}>Editar</button>
                  <button
                    className="text-red-600 underline text-xs"
                    onClick={() => { if (confirm(`Excluir tipo "${t.nome}"?`)) removeMutation.mutate(t.id); }}
                  >Excluir</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <Modal
        open={open}
        title={editing ? 'Editar tipo de incidente' : 'Novo tipo de incidente'}
        onClose={() => setOpen(false)}
        footer={
          <>
            <button className="btn-secondary" onClick={() => setOpen(false)}>Cancelar</button>
            <button
              className="btn-primary"
              onClick={handleSubmit((d) => saveMutation.mutate(d))}
              disabled={saveMutation.isPending}
            >Salvar</button>
          </>
        }
      >
        <form>
          <FormField label="Nome">
            <input className="input" {...register('nome', { required: true })} />
          </FormField>
          <FormField label="Descricao">
            <textarea className="input" rows={3} {...register('descricao')} />
          </FormField>
          <FormField label="Nivel de gravidade">
            <select className="input" {...register('nivelGravidade', { required: true })}>
              {NIVEIS.map((n) => <option key={n} value={n}>{n}</option>)}
            </select>
          </FormField>
          {error && <div className="mt-3 p-2 rounded bg-red-50 border border-red-200 text-sm text-red-700">{error}</div>}
        </form>
      </Modal>
    </div>
  );
}
