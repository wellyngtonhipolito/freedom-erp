-- ==============================================================================
-- V1_5__estoque_compras_completo.sql
-- Freedom ERP - Módulo Estoque & Compras (Antigo GMS):
-- 1. Almoxarifados / Locais de Estoque
-- 2. Saldo e Posição de Estoque por Produto/Almoxarifado
-- 3. Movimentações de Entrada, Saída e Transferência de Estoque
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- 1. Almoxarifados (Locais de Armazenamento)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS estoque_compras.almoxarifado (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    filial_id BIGINT NOT NULL REFERENCES configuracao.filial(id) ON DELETE RESTRICT,
    codigo_almoxarifado INTEGER NOT NULL,
    descricao VARCHAR(60) NOT NULL, -- Ex: 'Almoxarifado Central', 'Depósito Varejo'
    padrao BOOLEAN NOT NULL DEFAULT FALSE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_almoxarifado_codigo UNIQUE (filial_id, codigo_almoxarifado)
);

-- Carga inicial do almoxarifado principal
INSERT INTO estoque_compras.almoxarifado (filial_id, codigo_almoxarifado, descricao, padrao)
VALUES (1, 1, 'Almoxarifado Central (Matriz)', TRUE)
ON CONFLICT DO NOTHING;

-- ------------------------------------------------------------------------------
-- 2. Saldo Atual de Estoque por Produto e Almoxarifado
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS estoque_compras.saldo_estoque (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    almoxarifado_id BIGINT NOT NULL REFERENCES estoque_compras.almoxarifado(id) ON DELETE RESTRICT,
    produto_id BIGINT NOT NULL REFERENCES comercial.produto(id) ON DELETE RESTRICT,
    quantidade_atual NUMERIC(15, 4) NOT NULL DEFAULT 0.0000,
    quantidade_reservada NUMERIC(15, 4) NOT NULL DEFAULT 0.0000,
    custo_medio_ponderado NUMERIC(15, 4) NOT NULL DEFAULT 0.0000,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_saldo_almox_prod UNIQUE (almoxarifado_id, produto_id)
);

CREATE INDEX IF NOT EXISTS idx_saldo_estoque_prod ON estoque_compras.saldo_estoque(produto_id);

-- ------------------------------------------------------------------------------
-- 3. Movimentações de Estoque (Kardex / Extrato)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS estoque_compras.movimento_estoque (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    almoxarifado_id BIGINT NOT NULL REFERENCES estoque_compras.almoxarifado(id) ON DELETE RESTRICT,
    produto_id BIGINT NOT NULL REFERENCES comercial.produto(id) ON DELETE RESTRICT,
    tipo_movimento VARCHAR(10) NOT NULL, -- 'ENTRADA', 'SAIDA' ou 'AJUSTE'
    origem_movimento VARCHAR(30) NOT NULL DEFAULT 'MANUAL', -- COMPRA, VENDA, TRANSFERENCIA, AJUSTE_INVENTARIO
    documento_referencia VARCHAR(40),
    data_movimento DATE NOT NULL DEFAULT CURRENT_DATE,
    
    quantidade NUMERIC(15, 4) NOT NULL,
    custo_unitario NUMERIC(15, 4) NOT NULL,
    valor_total NUMERIC(15, 2) NOT NULL,
    
    quantidade_apos_movimento NUMERIC(15, 4) NOT NULL,
    custo_medio_apos_movimento NUMERIC(15, 4) NOT NULL,
    
    observacao TEXT,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_mov_estoque_prod_data ON estoque_compras.movimento_estoque(produto_id, data_movimento);

