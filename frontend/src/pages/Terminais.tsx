import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { terminalService, type TerminalForm } from '../services/terminalService';
import type { Terminal } from '../types/api';
import { extractApiError } from '../services/api';
import PageHeader from '../components/PageHeader';
import Modal from '../components/Modal';
import FormField from '../components/FormField';
import { useAuth } from '../contexts/AuthContext';

export default function Terminais() {
  const { hasRole } = useAuth();
  const isAdmin = hasRole('ADMINISTRADOR');
  const qc = useQueryClient();
  const [editing, setEditing] = useState<Terminal | null>(null);
  const [open, setOpen] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const { data, isLoading } = useQuery({
    queryKey: ['terminais'],
    queryFn: () => terminalService.list(0, 100),
  });

  const { register, handleSubmit, reset, formState: { isSubmitting } } = useForm<TerminalForm>();

  const openCreate = () => {
    setEditing(null);
    reset({ nome: '', endereco: '', cidade: '', ativo: true });
    setError(null);
    setOpen(true);
  };

  const openEdit = (t: Terminal) => {
    setEditing(t);
    reset({ nome: t.nome, endereco: t.endereco ?? '', cidade: t.cidade, ativo: t.ativo });
    setError(null);
    setOpen(true);
  };

  const saveMutation = useMutation({
    mutationFn: (form: TerminalForm) =>
      editing ? terminalService.update(editing.id, form) : terminalService.create(form),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['terminais'] });
      setOpen(false);
    },
    onError: (err) => setError(extractApiError(err)),
  });

  const removeMutation = useMutation({
    mutationFn: (id: number) => terminalService.remove(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['terminais'] }),
    onError: (err) => alert(extractApiError(err)),
  });

  return (
    <div>
      <PageHeader
        title="Terminais"
        description="Cadastro dos terminais operados pela City Transporte."
        actions={
          isAdmin && (
            <button onClick={openCreate} className="btn-primary">
              Novo terminal
            </button>
          )
        }
      />

      <div className="card overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="text-left text-slate-500 border-b">
            <tr>
              <th className="py-2">#</th>
              <th>Nome</th>
              <th>Cidade</th>
              <th>Endereco</th>
              <th>Ativo</th>
              {isAdmin && <th></th>}
            </tr>
          </thead>
          <tbody>
            {isLoading && (
              <tr><td colSpan={6} className="py-3 text-slate-400">Carregando...</td></tr>
            )}
            {data?.content.map((t) => (
              <tr key={t.id} className="border-b last:border-0">
                <td className="py-2">{t.id}</td>
                <td>{t.nome}</td>
                <td>{t.cidade}</td>
                <td className="text-slate-500">{t.endereco ?? '-'}</td>
                <td>{t.ativo ? 'Sim' : 'Nao'}</td>
                {isAdmin && (
                  <td className="text-right space-x-2">
                    <button className="text-brand underline text-xs" onClick={() => openEdit(t)}>
                      Editar
                    </button>
                    <button
                      className="text-red-600 underline text-xs"
                      onClick={() => {
                        if (confirm(`Excluir terminal "${t.nome}"?`)) removeMutation.mutate(t.id);
                      }}
                    >
                      Excluir
                    </button>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <Modal
        open={open}
        title={editing ? 'Editar terminal' : 'Novo terminal'}
        onClose={() => setOpen(false)}
        footer={
          <>
            <button className="btn-secondary" onClick={() => setOpen(false)}>Cancelar</button>
            <button
              className="btn-primary"
              onClick={handleSubmit((d) => saveMutation.mutate(d))}
              disabled={isSubmitting || saveMutation.isPending}
            >
              {saveMutation.isPending ? 'Salvando...' : 'Salvar'}
            </button>
          </>
        }
      >
        <form>
          <FormField label="Nome">
            <input className="input" {...register('nome', { required: true })} />
          </FormField>
          <FormField label="Cidade">
            <input className="input" {...register('cidade', { required: true })} />
          </FormField>
          <FormField label="Endereco">
            <input className="input" {...register('endereco')} />
          </FormField>
          <label className="flex items-center gap-2 text-sm text-slate-700">
            <input type="checkbox" {...register('ativo')} /> Ativo
          </label>
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
