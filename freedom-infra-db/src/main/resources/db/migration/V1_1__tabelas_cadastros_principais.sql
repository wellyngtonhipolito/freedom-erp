-- ==============================================================================
-- V1_1__tabelas_cadastros_principais.sql
-- Freedom ERP - Tabelas Centrais de Usuários, Empresas, Filiais, Pessoas e Produtos
-- Substitui Firebird generators por IDENTITY columns e CHAR(1) por BOOLEAN
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- 1. CFG: Empresa e Filial
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS cfg.empresa (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    razao_social VARCHAR(120) NOT NULL,
    nome_fantasia VARCHAR(80),
    cnpj VARCHAR(18) NOT NULL UNIQUE,
    inscricao_estadual VARCHAR(30),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cfg.filial (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES cfg.empresa(id) ON DELETE RESTRICT,
    codigo_filial INTEGER NOT NULL,
    razao_social VARCHAR(120) NOT NULL,
    nome_fantasia VARCHAR(80),
    cnpj VARCHAR(18) NOT NULL,
    inscricao_estadual VARCHAR(30),
    regime_tributario VARCHAR(30) NOT NULL DEFAULT 'SIMPLES_NACIONAL', -- SIMPLES_NACIONAL, LUCRO_PRESUMIDO, LUCRO_REAL
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_filial_empresa_codigo UNIQUE (empresa_id, codigo_filial)
);

-- ------------------------------------------------------------------------------
-- 2. STD: Pessoa Unificada (Clientes, Fornecedores, Transportadoras e Vendedores)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS std.pessoa (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    filial_id BIGINT NOT NULL REFERENCES cfg.filial(id) ON DELETE RESTRICT,
    tipo_pessoa VARCHAR(2) NOT NULL DEFAULT 'PJ', -- 'PF' ou 'PJ'
    razao_social VARCHAR(120) NOT NULL,
    nome_fantasia VARCHAR(80),
    cpf_cnpj VARCHAR(18) NOT NULL,
    inscricao_estadual VARCHAR(30),
    inscricao_municipal VARCHAR(30),
    
    -- Papéis assumidos pela pessoa no sistema
    eh_cliente BOOLEAN NOT NULL DEFAULT FALSE,
    eh_fornecedor BOOLEAN NOT NULL DEFAULT FALSE,
    eh_transportadora BOOLEAN NOT NULL DEFAULT FALSE,
    eh_vendedor BOOLEAN NOT NULL DEFAULT FALSE,

    -- Endereço principal
    cep VARCHAR(9),
    logradouro VARCHAR(100),
    numero VARCHAR(20),
    complemento VARCHAR(60),
    bairro VARCHAR(60),
    municipio_ibge VARCHAR(7),
    nome_municipio VARCHAR(80),
    uf VARCHAR(2),

    -- Contato
    telefone VARCHAR(20),
    celular_whatsapp VARCHAR(20),
    email VARCHAR(100),

    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_pessoa_cpf_cnpj ON std.pessoa(cpf_cnpj);
CREATE INDEX IF NOT EXISTS idx_pessoa_razao_social_trgm ON std.pessoa USING gin (razao_social gin_trgm_ops);

-- ------------------------------------------------------------------------------
-- 3. STD: Produtos e Itens Fiscais
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS std.produto (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    filial_id BIGINT NOT NULL REFERENCES cfg.filial(id) ON DELETE RESTRICT,
    codigo_referencia VARCHAR(30),
    codigo_barras_ean VARCHAR(14),
    descricao VARCHAR(120) NOT NULL,
    unidade_medida VARCHAR(6) NOT NULL DEFAULT 'UN',
    
    -- Preços e Custos
    preco_custo NUMERIC(15, 4) NOT NULL DEFAULT 0.0000,
    preco_venda NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    markup_percentual NUMERIC(7, 2) NOT NULL DEFAULT 0.00,

    -- Classificações Fiscais Obrigatórias
    ncm VARCHAR(8) NOT NULL,
    cest VARCHAR(7),
    origem_mercadoria INTEGER NOT NULL DEFAULT 0, -- 0: Nacional, 1: Importação Direta, etc.
    tipo_item_sped VARCHAR(2) NOT NULL DEFAULT '00', -- 00: Mercadoria para Revenda, 01: Matéria-Prima, etc.
    
    -- Transição Tributária 2026 (EC 132/23)
    sujeito_imposto_seletivo BOOLEAN NOT NULL DEFAULT FALSE,

    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_produto_ean ON std.produto(codigo_barras_ean);
CREATE INDEX IF NOT EXISTS idx_produto_descricao_trgm ON std.produto USING gin (descricao gin_trgm_ops);

