CREATE TABLE terminal (
    id_terminal BIGSERIAL PRIMARY KEY,
    nm_terminal VARCHAR(100) NOT NULL,
    ds_endereco VARCHAR(200),
    nm_cidade VARCHAR(80) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE doca (
    id_doca BIGSERIAL PRIMARY KEY,
    id_terminal BIGINT NOT NULL REFERENCES terminal(id_terminal),
    cd_doca VARCHAR(20) NOT NULL,
    ds_localizacao VARCHAR(150),
    status_doca VARCHAR(30) NOT NULL DEFAULT 'DISPONIVEL',

    CONSTRAINT ck_doca_status
        CHECK (status_doca IN ('DISPONIVEL', 'OCUPADA', 'MANUTENCAO', 'INTERDITADA'))
);

CREATE TABLE estacionamento (
    id_estacionamento BIGSERIAL PRIMARY KEY,
    id_terminal BIGINT NOT NULL REFERENCES terminal(id_terminal),
    nm_estacionamento VARCHAR(100) NOT NULL,
    capacidade INTEGER NOT NULL,
    status_estacionamento VARCHAR(30) NOT NULL DEFAULT 'ATIVO',

    CONSTRAINT ck_estacionamento_capacidade
        CHECK (capacidade > 0),

    CONSTRAINT ck_estacionamento_status
        CHECK (status_estacionamento IN ('ATIVO', 'LOTADO', 'MANUTENCAO', 'INTERDITADO'))
);

CREATE TABLE vaga_estacionamento (
    id_vaga BIGSERIAL PRIMARY KEY,
    id_estacionamento BIGINT NOT NULL REFERENCES estacionamento(id_estacionamento),
    cd_vaga VARCHAR(20) NOT NULL,
    status_vaga VARCHAR(30) NOT NULL DEFAULT 'LIVRE',

    CONSTRAINT ck_vaga_status
        CHECK (status_vaga IN ('LIVRE', 'OCUPADA', 'RESERVADA', 'INTERDITADA'))
);

CREATE TABLE veiculo (
    id_veiculo BIGSERIAL PRIMARY KEY,
    nr_placa VARCHAR(10) NOT NULL UNIQUE,
    tp_veiculo VARCHAR(40) NOT NULL,
    nm_empresa_responsavel VARCHAR(150) NOT NULL,
    tp_empresa_responsavel VARCHAR(30) NOT NULL,
    ds_modelo VARCHAR(80),

    CONSTRAINT ck_veiculo_tipo
        CHECK (tp_veiculo IN ('CAMINHAO', 'CARRETA', 'VAN', 'UTILITARIO', 'OUTRO')),

    CONSTRAINT ck_empresa_responsavel_tipo
        CHECK (tp_empresa_responsavel IN ('TRANSPORTADORA', 'CLIENTE', 'FORNECEDOR', 'OPERADORA'))
);

CREATE TABLE usuario (
    id_usuario BIGSERIAL PRIMARY KEY,
    nm_usuario VARCHAR(100) NOT NULL,
    ds_email VARCHAR(120) UNIQUE,
    perfil VARCHAR(40) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT ck_usuario_perfil
        CHECK (perfil IN ('ADMINISTRADOR', 'OPERADOR', 'SUPERVISOR', 'SEGURANCA'))
);

CREATE TABLE operacao_carga (
    id_operacao BIGSERIAL PRIMARY KEY,
    id_terminal BIGINT     NOT NULL REFERENCES terminal(id_terminal),
    id_doca BIGINT     REFERENCES doca(id_doca),
    id_vaga BIGINT     REFERENCES vaga_estacionamento(id_vaga),
    id_veiculo BIGINT     REFERENCES veiculo(id_veiculo),
    id_usuario BIGINT     REFERENCES usuario(id_usuario),

    tp_operacao VARCHAR(30) NOT NULL,
    status_operacao VARCHAR(30) NOT NULL DEFAULT 'AGENDADA',

    ds_carga VARCHAR(200),
    qtd_volume INTEGER,
    peso_estimado NUMERIC(10,2),

    dt_agendada TIMESTAMP,
    dt_inicio TIMESTAMP,
    dt_fim TIMESTAMP,

    observacao TEXT,

    CONSTRAINT ck_operacao_tipo
        CHECK (tp_operacao IN ('CARGA', 'DESCARGA')),

    CONSTRAINT ck_operacao_status
        CHECK (status_operacao IN ('AGENDADA', 'EM_ANDAMENTO', 'FINALIZADA', 'CANCELADA')),

    CONSTRAINT ck_operacao_qtd_volume
        CHECK (qtd_volume IS NULL OR qtd_volume >= 0),

    CONSTRAINT ck_operacao_peso
        CHECK (peso_estimado IS NULL OR peso_estimado >= 0),

    CONSTRAINT ck_operacao_datas
        CHECK (
            dt_fim IS NULL 
            OR dt_inicio IS NULL 
            OR dt_fim >= dt_inicio
        )
);

CREATE TABLE documento_carga (
    id_documento BIGSERIAL PRIMARY KEY,
    id_operacao BIGINT NOT NULL REFERENCES operacao_carga(id_operacao),
    tp_documento VARCHAR(50) NOT NULL,
    nr_documento VARCHAR(80) NOT NULL,
    dt_emissao DATE,
    observacao TEXT,

    CONSTRAINT ck_documento_tipo
        CHECK (tp_documento IN ('NOTA_FISCAL', 'ROMANEIO', 'CONHECIMENTO_TRANSPORTE', 'OUTRO'))
);

CREATE TABLE tipo_incidente (
    id_tipo_incidente BIGSERIAL PRIMARY KEY,
    nm_tipo_incidente VARCHAR(100) NOT NULL UNIQUE,
    ds_tipo_incidente TEXT,
    nivel_gravidade VARCHAR(30) NOT NULL DEFAULT 'BAIXO',

    CONSTRAINT ck_tipo_incidente_gravidade
        CHECK (nivel_gravidade IN ('BAIXO', 'MEDIO', 'ALTO', 'CRITICO'))
);

CREATE TABLE incidente (
    id_incidente BIGSERIAL PRIMARY KEY,
    id_tipo_incidente BIGINT NOT NULL REFERENCES tipo_incidente(id_tipo_incidente),
    id_terminal BIGINT NOT NULL REFERENCES terminal(id_terminal),
    id_doca BIGINT REFERENCES doca(id_doca),
    id_estacionamento BIGINT REFERENCES estacionamento(id_estacionamento),
    id_vaga BIGINT REFERENCES vaga_estacionamento(id_vaga),
    id_operacao BIGINT REFERENCES operacao_carga(id_operacao),
    id_usuario_registro BIGINT REFERENCES usuario(id_usuario),

    dt_incidente TIMESTAMP NOT NULL,
    ds_incidente TEXT NOT NULL,
    status_incidente VARCHAR(30) NOT NULL DEFAULT 'ABERTO',
    acao_tomada TEXT,
    dt_encerramento TIMESTAMP,

    CONSTRAINT ck_incidente_status
        CHECK (status_incidente IN ('ABERTO', 'EM_ANALISE', 'RESOLVIDO', 'CANCELADO')),

    CONSTRAINT ck_incidente_datas
        CHECK (
            dt_encerramento IS NULL 
            OR dt_encerramento >= dt_incidente
        )
);