import { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { operacaoService, type DocumentoForm } from '../services/operacaoService';
import type { TipoDocumento } from '../types/api';
import { extractApiError } from '../services/api';
import PageHeader from '../components/PageHeader';
import StatusBadge from '../components/StatusBadge';
import FormField from '../components/FormField';
import { useAuth } from '../contexts/AuthContext';

const TIPOS_DOC: TipoDocumento[] = ['NOTA_FISCAL', 'ROMANEIO', 'CONHECIMENTO_TRANSPORTE', 'OUTRO'];

function formatDate(iso?: string) {
  if (!iso) return '-';
  return new Date(iso).toLocaleString('pt-BR');
}

export default function OperacaoDetalhe() {
  const { id } = useParams<{ id: string }>();
  const operacaoId = Number(id);
  const qc = useQueryClient();
  const { hasRole } = useAuth();
  const canAddDoc = hasRole('OPERADOR', 'SUPERVISOR', 'ADMINISTRADOR');
  const canRemoveDoc = hasRole('SUPERVISOR', 'ADMINISTRADOR');

  const [docError, setDocError] = useState<string | null>(null);

  const operacao = useQuery({
    queryKey: ['operacao', operacaoId],
    queryFn: () => operacaoService.findById(operacaoId),
    enabled: !!operacaoId,
  });

  const actionMutation = useMutation({
    mutationFn: (action: 'iniciar' | 'finalizar' | 'checkout' | 'cancelar') => {
      if (action === 'iniciar') return operacaoService.iniciar(operacaoId);
      if (action === 'finalizar') return operacaoService.finalizar(operacaoId);
      if (action === 'checkout') return operacaoService.checkout(operacaoId);
      return operacaoService.cancelar(operacaoId);
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: ['operacao', operacaoId] }),
    onError: (err) => alert(extractApiError(err)),
  });

  const { register, handleSubmit, reset } = useForm<DocumentoForm>({
    defaultValues: { tipo: 'NOTA_FISCAL', numero: '' },
  });

  const addDocMutation = useMutation({
    mutationFn: (d: DocumentoForm) => operacaoService.addDocumento(operacaoId, d),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['operacao', operacaoId] });
      reset({ tipo: 'NOTA_FISCAL', numero: '', emitidoEm: '', observacao: '' });
      setDocError(null);
    },
    onError: (err) => setDocError(extractApiError(err)),
  });

  const removeDocMutation = useMutation({
    mutationFn: (docId: number) => operacaoService.removeDocumento(docId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['operacao', operacaoId] }),
    onError: (err) => alert(extractApiError(err)),
  });

  if (operacao.isLoading) return <div className="text-slate-400">Carregando...</div>;
  if (!operacao.data) return <div className="text-red-600">Operacao nao encontrada</div>;

  const o = operacao.data;
  const canIniciar = o.status === 'AGENDADA' && hasRole('OPERADOR', 'SUPERVISOR', 'ADMINISTRADOR');
  const canFinalizar = o.status === 'EM_ANDAMENTO' && hasRole('OPERADOR', 'SUPERVISOR', 'ADMINISTRADOR');
  const canCheckout = o.status === 'FINALIZADA' && hasRole('OPERADOR', 'SEGURANCA', 'ADMINISTRADOR');
  const canCancelar = o.status !== 'FINALIZADA' && o.status !== 'CANCELADA' && hasRole('SUPERVISOR', 'ADMINISTRADOR');

  return (
    <div>
      <PageHeader
        title={`Operacao #${o.id}`}
        description={`${o.tipo} - ${o.terminalNome}`}
        actions={
          <>
            <Link to="/operacoes" className="btn-secondary">Voltar</Link>
            {canIniciar && <button className="btn-primary" onClick={() => actionMutation.mutate('iniciar')}>Iniciar</button>}
            {canFinalizar && <button className="btn-primary" onClick={() => actionMutation.mutate('finalizar')}>Finalizar</button>}
            {canCheckout && <button className="btn-primary" onClick={() => actionMutation.mutate('checkout')}>Checkout</button>}
            {canCancelar && (
              <button
                className="btn-secondary text-red-700"
                onClick={() => { if (confirm('Cancelar operacao?')) actionMutation.mutate('cancelar'); }}
              >Cancelar</button>
            )}
          </>
        }
      />

      <div className="grid md:grid-cols-2 gap-4 mb-6">
        <div className="card">
          <h3 className="font-semibold text-slate-700 mb-3">Dados</h3>
          <dl className="text-sm space-y-2">
            <div className="flex justify-between"><dt className="text-slate-500">Status</dt><dd><StatusBadge value={o.status} /></dd></div>
            <div className="flex justify-between"><dt className="text-slate-500">Veiculo</dt><dd className="font-semibold">{o.veiculoPlaca ?? '-'}</dd></div>
            <div className="flex justify-between"><dt className="text-slate-500">Doca</dt><dd>{o.docaCodigo ?? '-'}</dd></div>
            <div className="flex justify-between"><dt className="text-slate-500">Vaga</dt><dd>{o.vagaCodigo ?? '-'}</dd></div>
            <div className="flex justify-between"><dt className="text-slate-500">Operador</dt><dd>{o.usuarioNome ?? '-'}</dd></div>
            <div className="flex justify-between"><dt className="text-slate-500">Descricao</dt><dd className="text-right max-w-xs">{o.descricaoCarga ?? '-'}</dd></div>
            <div className="flex justify-between"><dt className="text-slate-500">Volume</dt><dd>{o.quantidadeVolume ?? '-'}</dd></div>
            <div className="flex justify-between"><dt className="text-slate-500">Peso (kg)</dt><dd>{o.pesoEstimado ?? '-'}</dd></div>
            {o.observacao && (
              <div className="pt-2 border-t mt-2">
                <dt className="text-slate-500 mb-1">Observacao</dt>
                <dd className="text-slate-700">{o.observacao}</dd>
              </div>
            )}
          </dl>
        </div>

        <div className="card">
          <h3 className="font-semibold text-slate-700 mb-3">Linha do tempo</h3>
          <ol className="relative border-l border-slate-200 ml-2 space-y-4 text-sm">
            <li className="ml-4">
              <div className="absolute w-3 h-3 bg-blue-500 rounded-full -left-1.5 mt-1"></div>
              <p className="font-semibold">Agendada</p>
              <p className="text-xs text-slate-500">{formatDate(o.agendadaEm)}</p>
            </li>
            <li className="ml-4">
              <div className={`absolute w-3 h-3 ${o.iniciadaEm ? 'bg-amber-500' : 'bg-slate-300'} rounded-full -left-1.5 mt-1`}></div>
              <p className="font-semibold">Iniciada</p>
              <p className="text-xs text-slate-500">{formatDate(o.iniciadaEm)}</p>
            </li>
            <li className="ml-4">
              <div className={`absolute w-3 h-3 ${o.finalizadaEm ? 'bg-green-500' : 'bg-slate-300'} rounded-full -left-1.5 mt-1`}></div>
              <p className="font-semibold">Finalizada</p>
              <p className="text-xs text-slate-500">{formatDate(o.finalizadaEm)}</p>
            </li>
          </ol>
        </div>
      </div>

      <div className="card">
        <h3 className="font-semibold text-slate-700 mb-3">Documentos de carga</h3>

        {o.documentos.length === 0 ? (
          <p className="text-sm text-slate-400 mb-4">Nenhum documento vinculado.</p>
        ) : (
          <table className="w-full text-sm mb-4">
            <thead className="text-left text-slate-500 border-b">
              <tr>
                <th className="py-2">Tipo</th>
                <th>Numero</th>
                <th>Emitido em</th>
                <th>Observacao</th>
                {canRemoveDoc && <th></th>}
              </tr>
            </thead>
            <tbody>
              {o.documentos.map((d) => (
                <tr key={d.id} className="border-b last:border-0">
                  <td className="py-2">{d.tipo.replace(/_/g, ' ')}</td>
                  <td className="font-semibold">{d.numero}</td>
                  <td className="text-xs text-slate-500">{d.emitidoEm ? new Date(d.emitidoEm).toLocaleDateString('pt-BR') : '-'}</td>
                  <td className="text-slate-500 text-xs">{d.observacao ?? '-'}</td>
                  {canRemoveDoc && (
                    <td className="text-right">
                      <button
                        className="text-red-600 underline text-xs"
                        onClick={() => { if (confirm('Remover documento?')) removeDocMutation.mutate(d.id); }}
                      >Remover</button>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        )}

        {canAddDoc && (
          <form
            className="border-t pt-4"
            onSubmit={handleSubmit((d) => addDocMutation.mutate(d))}
          >
            <h4 className="text-sm font-semibold text-slate-700 mb-2">Adicionar documento</h4>
            <div className="grid md:grid-cols-2 gap-3">
              <FormField label="Tipo">
                <select className="input" {...register('tipo', { required: true })}>
                  {TIPOS_DOC.map((t) => <option key={t} value={t}>{t.replace(/_/g, ' ')}</option>)}
                </select>
              </FormField>
              <FormField label="Numero">
                <input className="input" {...register('numero', { required: true })} />
              </FormField>
              <FormField label="Emitido em">
                <input className="input" type="date" {...register('emitidoEm')} />
              </FormField>
              <FormField label="Observacao">
                <input className="input" {...register('observacao')} />
              </FormField>
            </div>
            {docError && <div className="mt-2 p-2 rounded bg-red-50 border border-red-200 text-sm text-red-700">{docError}</div>}
            <button type="submit" className="btn-primary mt-2" disabled={addDocMutation.isPending}>
              Adicionar
            </button>
          </form>
        )}
      </div>
    </div>
  );
}
