-- ==============================================================================
-- V1_6__producao_completo.sql
-- Freedom ERP - Módulo de Produção & PCP (Antigo PCP):
-- 1. Estrutura do Produto (Ficha Técnica / BOM - Bill of Materials)
-- 2. Ordem de Produção (OP)
-- 3. Apontamento e Consumo de Insumos
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- 1. Estrutura de Produtos (BOM - Ficha Técnica / Receita de Fabricação)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS producao.estrutura_produto (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    produto_pai_id BIGINT NOT NULL REFERENCES comercial.produto(id) ON DELETE CASCADE,
    componente_id BIGINT NOT NULL REFERENCES comercial.produto(id) ON DELETE RESTRICT,
    quantidade_necessaria NUMERIC(15, 4) NOT NULL DEFAULT 1.0000,
    percentual_perda NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_estrutura_pai_componente UNIQUE (produto_pai_id, componente_id)
);

CREATE INDEX IF NOT EXISTS idx_estrutura_pai ON producao.estrutura_produto(produto_pai_id);

-- ------------------------------------------------------------------------------
-- 2. Ordem de Produção (OP)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS producao.ordem_producao (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    filial_id BIGINT NOT NULL REFERENCES configuracao.filial(id) ON DELETE RESTRICT,
    numero_op VARCHAR(30) NOT NULL UNIQUE,
    produto_id BIGINT NOT NULL REFERENCES comercial.produto(id) ON DELETE RESTRICT,
    almoxarifado_id BIGINT NOT NULL REFERENCES estoque_compras.almoxarifado(id) ON DELETE RESTRICT,
    
    quantidade_planejada NUMERIC(15, 4) NOT NULL,
    quantidade_produzida NUMERIC(15, 4) NOT NULL DEFAULT 0.0000,
    
    data_emissao DATE NOT NULL DEFAULT CURRENT_DATE,
    data_previsao_fim DATE,
    data_finalizacao DATE,
    
    custo_total_fabricacao NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    
    -- Ciclo: PLANEJADA -> EM_PROCESSO -> FINALIZADA -> CANCELADA
    status_op VARCHAR(20) NOT NULL DEFAULT 'PLANEJADA',
    observacao TEXT,
    
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_op_produto ON producao.ordem_producao(produto_id);
CREATE INDEX IF NOT EXISTS idx_op_status ON producao.ordem_producao(status_op);
