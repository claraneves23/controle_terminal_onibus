import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { veiculoService, type VeiculoForm } from '../services/veiculoService';
import type { TipoEmpresa, TipoVeiculo, Veiculo } from '../types/api';
import { extractApiError } from '../services/api';
import PageHeader from '../components/PageHeader';
import Modal from '../components/Modal';
import FormField from '../components/FormField';
import { useAuth } from '../contexts/AuthContext';

const TIPOS_VEICULO: TipoVeiculo[] = ['CAMINHAO', 'CARRETA', 'VAN', 'UTILITARIO', 'OUTRO'];
const TIPOS_EMPRESA: TipoEmpresa[] = ['TRANSPORTADORA', 'CLIENTE', 'FORNECEDOR', 'OPERADORA'];

export default function Veiculos() {
  const { hasRole } = useAuth();
  const canEdit = hasRole('ADMINISTRADOR', 'SUPERVISOR', 'OPERADOR');
  const isAdmin = hasRole('ADMINISTRADOR');
  const qc = useQueryClient();
  const [busca, setBusca] = useState('');
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<Veiculo | null>(null);
  const [error, setError] = useState<string | null>(null);

  const veiculos = useQuery({
    queryKey: ['veiculos', busca],
    queryFn: () => veiculoService.list({ placa: busca || undefined, size: 50 }),
  });

  const { register, handleSubmit, reset } = useForm<VeiculoForm>();

  const openCreate = () => {
    setEditing(null);
    reset({ placa: '', tipo: 'CAMINHAO', empresaResponsavel: '', tipoEmpresa: 'TRANSPORTADORA', modelo: '' });
    setError(null);
    setOpen(true);
  };

  const openEdit = (v: Veiculo) => {
    setEditing(v);
    reset({
      placa: v.placa,
      tipo: v.tipo,
      empresaResponsavel: v.empresaResponsavel,
      tipoEmpresa: v.tipoEmpresa,
      modelo: v.modelo ?? '',
    });
    setError(null);
    setOpen(true);
  };

  const saveMutation = useMutation({
    mutationFn: (d: VeiculoForm) =>
      editing ? veiculoService.update(editing.id, d) : veiculoService.create(d),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['veiculos'] }); setOpen(false); },
    onError: (err) => setError(extractApiError(err)),
  });

  const removeMutation = useMutation({
    mutationFn: (id: number) => veiculoService.remove(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['veiculos'] }),
    onError: (err) => alert(extractApiError(err)),
  });

  return (
    <div>
      <PageHeader
        title="Veiculos"
        description="Cadastro dos veiculos que operam no terminal."
        actions={canEdit && <button className="btn-primary" onClick={openCreate}>Novo veiculo</button>}
      />

      <div className="card mb-4">
        <input
          className="input max-w-md"
          placeholder="Buscar por placa..."
          value={busca}
          onChange={(e) => setBusca(e.target.value)}
        />
      </div>

      <div className="card overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="text-left text-slate-500 border-b">
            <tr>
              <th className="py-2">Placa</th>
              <th>Tipo</th>
              <th>Empresa</th>
              <th>Tipo Empresa</th>
              <th>Modelo</th>
              {canEdit && <th></th>}
            </tr>
          </thead>
          <tbody>
            {veiculos.data?.content.map((v) => (
              <tr key={v.id} className="border-b last:border-0">
                <td className="py-2 font-semibold">{v.placa}</td>
                <td>{v.tipo}</td>
                <td>{v.empresaResponsavel}</td>
                <td className="text-xs text-slate-500">{v.tipoEmpresa}</td>
                <td>{v.modelo ?? '-'}</td>
                {canEdit && (
                  <td className="text-right space-x-2">
                    <button className="text-brand underline text-xs" onClick={() => openEdit(v)}>Editar</button>
                    {isAdmin && (
                      <button
                        className="text-red-600 underline text-xs"
                        onClick={() => { if (confirm(`Excluir ${v.placa}?`)) removeMutation.mutate(v.id); }}
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
        title={editing ? 'Editar veiculo' : 'Novo veiculo'}
        onClose={() => setOpen(false)}
        footer={
          <>
            <button className="btn-secondary" onClick={() => setOpen(false)}>Cancelar</button>
            <button
              className="btn-primary"
              onClick={handleSubmit((d) => saveMutation.mutate({ ...d, placa: d.placa.toUpperCase().trim() }))}
              disabled={saveMutation.isPending}
            >Salvar</button>
          </>
        }
      >
        <form>
          <FormField label="Placa (ex.: ABC1D23 ou ABC1234)">
            <input className="input uppercase" {...register('placa', { required: true })} />
          </FormField>
          <FormField label="Tipo de veiculo">
            <select className="input" {...register('tipo', { required: true })}>
              {TIPOS_VEICULO.map((t) => <option key={t} value={t}>{t}</option>)}
            </select>
          </FormField>
          <FormField label="Empresa responsavel">
            <input className="input" {...register('empresaResponsavel', { required: true })} />
          </FormField>
          <FormField label="Tipo da empresa">
            <select className="input" {...register('tipoEmpresa', { required: true })}>
              {TIPOS_EMPRESA.map((t) => <option key={t} value={t}>{t}</option>)}
            </select>
          </FormField>
          <FormField label="Modelo">
            <input className="input" {...register('modelo')} />
          </FormField>
          {error && <div className="mt-3 p-2 rounded bg-red-50 border border-red-200 text-sm text-red-700">{error}</div>}
        </form>
      </Modal>
    </div>
  );
}
