import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { usuarioService, type UsuarioForm } from '../services/usuarioService';
import type { Perfil, Usuario } from '../types/api';
import { extractApiError } from '../services/api';
import PageHeader from '../components/PageHeader';
import Modal from '../components/Modal';
import FormField from '../components/FormField';

const PERFIS: Perfil[] = ['ADMINISTRADOR', 'SUPERVISOR', 'OPERADOR', 'SEGURANCA'];

export default function Usuarios() {
  const qc = useQueryClient();
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<Usuario | null>(null);
  const [error, setError] = useState<string | null>(null);

  const usuarios = useQuery({
    queryKey: ['usuarios'],
    queryFn: () => usuarioService.list({ size: 100 }),
  });

  const { register, handleSubmit, reset } = useForm<UsuarioForm>();

  const openCreate = () => {
    setEditing(null);
    reset({ nome: '', email: '', perfil: 'OPERADOR', ativo: true, senha: '' });
    setError(null);
    setOpen(true);
  };

  const openEdit = (u: Usuario) => {
    setEditing(u);
    reset({ nome: u.nome, email: u.email, perfil: u.perfil, ativo: u.ativo, senha: '' });
    setError(null);
    setOpen(true);
  };

  const saveMutation = useMutation({
    mutationFn: (d: UsuarioForm) => {
      const payload = { ...d };
      if (editing && !payload.senha) delete payload.senha;
      return editing ? usuarioService.update(editing.id, payload) : usuarioService.create(payload);
    },
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['usuarios'] }); setOpen(false); },
    onError: (err) => setError(extractApiError(err)),
  });

  const removeMutation = useMutation({
    mutationFn: (id: number) => usuarioService.remove(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['usuarios'] }),
    onError: (err) => alert(extractApiError(err)),
  });

  return (
    <div>
      <PageHeader
        title="Usuarios"
        description="Operadores do sistema e seus perfis de acesso."
        actions={<button className="btn-primary" onClick={openCreate}>Novo usuario</button>}
      />

      <div className="card overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="text-left text-slate-500 border-b">
            <tr>
              <th className="py-2">Nome</th>
              <th>Email</th>
              <th>Perfil</th>
              <th>Ativo</th>
              <th>Ultimo login</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {usuarios.isLoading && <tr><td colSpan={6} className="py-3 text-slate-400">Carregando...</td></tr>}
            {usuarios.data?.content.map((u) => (
              <tr key={u.id} className="border-b last:border-0">
                <td className="py-2 font-semibold">{u.nome}</td>
                <td className="text-slate-600">{u.email}</td>
                <td><span className="text-xs px-2 py-0.5 rounded bg-slate-100 text-slate-700">{u.perfil}</span></td>
                <td>{u.ativo ? 'Sim' : 'Nao'}</td>
                <td className="text-xs text-slate-500">{u.ultimoLogin ? new Date(u.ultimoLogin).toLocaleString('pt-BR') : '-'}</td>
                <td className="text-right space-x-2">
                  <button className="text-brand underline text-xs" onClick={() => openEdit(u)}>Editar</button>
                  <button
                    className="text-red-600 underline text-xs"
                    onClick={() => { if (confirm(`Excluir usuario "${u.nome}"?`)) removeMutation.mutate(u.id); }}
                  >Excluir</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <Modal
        open={open}
        title={editing ? 'Editar usuario' : 'Novo usuario'}
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
          <FormField label="Email">
            <input className="input" type="email" {...register('email', { required: true })} />
          </FormField>
          <FormField label="Perfil">
            <select className="input" {...register('perfil', { required: true })}>
              {PERFIS.map((p) => <option key={p} value={p}>{p}</option>)}
            </select>
          </FormField>
          <FormField label={editing ? 'Nova senha (deixe vazio para manter)' : 'Senha'}>
            <input className="input" type="password" {...register('senha', { required: !editing })} />
          </FormField>
          <label className="flex items-center gap-2 text-sm text-slate-700">
            <input type="checkbox" {...register('ativo')} /> Ativo
          </label>
          {error && <div className="mt-3 p-2 rounded bg-red-50 border border-red-200 text-sm text-red-700">{error}</div>}
        </form>
      </Modal>
    </div>
  );
}
