export type Perfil = 'ADMINISTRADOR' | 'OPERADOR' | 'SUPERVISOR' | 'SEGURANCA';

export type StatusDoca = 'DISPONIVEL' | 'OCUPADA' | 'MANUTENCAO' | 'INTERDITADA';
export type StatusEstacionamento = 'ATIVO' | 'LOTADO' | 'MANUTENCAO' | 'INTERDITADO';
export type StatusVaga = 'LIVRE' | 'OCUPADA' | 'RESERVADA' | 'INTERDITADA';
export type TipoVeiculo = 'CAMINHAO' | 'CARRETA' | 'VAN' | 'UTILITARIO' | 'OUTRO';
export type TipoEmpresa = 'TRANSPORTADORA' | 'CLIENTE' | 'FORNECEDOR' | 'OPERADORA';
export type TipoOperacao = 'CARGA' | 'DESCARGA';
export type StatusOperacao = 'AGENDADA' | 'EM_ANDAMENTO' | 'FINALIZADA' | 'CANCELADA';
export type TipoDocumento = 'NOTA_FISCAL' | 'ROMANEIO' | 'CONHECIMENTO_TRANSPORTE' | 'OUTRO';
export type NivelGravidade = 'BAIXO' | 'MEDIO' | 'ALTO' | 'CRITICO';
export type StatusIncidente = 'ABERTO' | 'EM_ANALISE' | 'RESOLVIDO' | 'CANCELADO';

export interface UsuarioLogado {
  id: number;
  nome: string;
  email: string;
  perfil: Perfil;
}

export interface TokenResponse {
  accessToken: string;
  tokenType: string;
  expiresInMs: number;
  usuario: UsuarioLogado;
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  code: string;
  message: string;
  path: string;
  details?: Array<{ field: string; rejectedValue?: unknown; message: string }>;
  traceId: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface Terminal {
  id: number;
  nome: string;
  endereco?: string;
  cidade: string;
  ativo: boolean;
}

export interface Doca {
  id: number;
  terminalId: number;
  terminalNome: string;
  codigo: string;
  localizacao?: string;
  status: StatusDoca;
}

export interface Estacionamento {
  id: number;
  terminalId: number;
  terminalNome: string;
  nome: string;
  capacidade: number;
  status: StatusEstacionamento;
}

export interface Vaga {
  id: number;
  estacionamentoId: number;
  estacionamentoNome: string;
  codigo: string;
  status: StatusVaga;
}

export interface Veiculo {
  id: number;
  placa: string;
  tipo: TipoVeiculo;
  empresaResponsavel: string;
  tipoEmpresa: TipoEmpresa;
  modelo?: string;
}

export interface Usuario {
  id: number;
  nome: string;
  email: string;
  perfil: Perfil;
  ativo: boolean;
  ultimoLogin?: string;
  criadoEm?: string;
}

export interface DocumentoCarga {
  id: number;
  operacaoId: number;
  tipo: TipoDocumento;
  numero: string;
  emitidoEm?: string;
  observacao?: string;
}

export interface OperacaoResumo {
  id: number;
  terminalId: number;
  terminalNome: string;
  docaCodigo?: string;
  vagaCodigo?: string;
  veiculoPlaca?: string;
  tipo: TipoOperacao;
  status: StatusOperacao;
  agendadaEm?: string;
  iniciadaEm?: string;
  finalizadaEm?: string;
}

export interface Operacao {
  id: number;
  terminalId: number;
  terminalNome: string;
  docaId?: number;
  docaCodigo?: string;
  vagaId?: number;
  vagaCodigo?: string;
  veiculoId?: number;
  veiculoPlaca?: string;
  usuarioId?: number;
  usuarioNome?: string;
  tipo: TipoOperacao;
  status: StatusOperacao;
  descricaoCarga?: string;
  quantidadeVolume?: number;
  pesoEstimado?: number;
  agendadaEm?: string;
  iniciadaEm?: string;
  finalizadaEm?: string;
  observacao?: string;
  documentos: DocumentoCarga[];
}

export interface TipoIncidente {
  id: number;
  nome: string;
  descricao?: string;
  nivelGravidade: NivelGravidade;
}

export interface Incidente {
  id: number;
  tipoIncidenteId: number;
  tipoIncidenteNome: string;
  nivelGravidade: NivelGravidade;
  terminalId: number;
  terminalNome: string;
  docaId?: number;
  docaCodigo?: string;
  estacionamentoId?: number;
  vagaId?: number;
  vagaCodigo?: string;
  operacaoId?: number;
  usuarioRegistroId?: number;
  usuarioRegistroNome?: string;
  ocorridoEm: string;
  descricao: string;
  status: StatusIncidente;
  acaoTomada?: string;
  encerradoEm?: string;
}

export interface Dashboard {
  totalDocas: number;
  docasOcupadas: number;
  taxaOcupacaoDocas: number;
  operacoesAgendadas: number;
  operacoesEmAndamento: number;
  operacoesFinalizadasHoje: number;
  veiculosNoPatio: number;
  tempoMedioSegundosHoje?: number;
  incidentesAbertos: number;
  incidentesAbertosPorGravidade: Record<NivelGravidade, number>;
  operacoesRecentes: OperacaoResumo[];
  docas: Doca[];
}
