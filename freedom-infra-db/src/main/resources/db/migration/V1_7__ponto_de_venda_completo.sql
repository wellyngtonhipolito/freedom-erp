-- ==============================================================================
-- V1_7__ponto_de_venda_completo.sql
-- Freedom ERP - Módulo Ponto de Venda & Frente de Caixa (Antigo PDV):
-- 1. Controle de Turno e Caixa (Abertura, Fechamento, Suprimento e Sangria)
-- 2. Venda Rápida de Balcão e Cupons (NFC-e)
-- 3. Itens da Venda Rápida
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- 1. Turno e Sessão de Caixa
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ponto_de_venda.caixa_sessao (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    filial_id BIGINT NOT NULL REFERENCES configuracao.filial(id) ON DELETE RESTRICT,
    numero_terminal INTEGER NOT NULL DEFAULT 1,
    operador_nome VARCHAR(60) NOT NULL,
    data_abertura TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_fechamento TIMESTAMPTZ,
    
    valor_fundo_troco NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    total_entradas NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    total_saidas_sangria NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    saldo_final_fechamento NUMERIC(15, 2),
    
    -- Status: ABERTO ou FECHADO
    status_caixa VARCHAR(20) NOT NULL DEFAULT 'ABERTO',
    observacao TEXT
);

CREATE INDEX IF NOT EXISTS idx_caixa_status ON ponto_de_venda.caixa_sessao(status_caixa);

-- ------------------------------------------------------------------------------
-- 2. Venda Rápida de Balcão (NFC-e)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ponto_de_venda.venda_pdv (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    caixa_sessao_id BIGINT NOT NULL REFERENCES ponto_de_venda.caixa_sessao(id) ON DELETE RESTRICT,
    numero_cupom VARCHAR(30) NOT NULL,
    data_venda TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Valores
    valor_subtotal NUMERIC(15, 2) NOT NULL,
    valor_desconto NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    valor_total NUMERIC(15, 2) NOT NULL,
    valor_pago NUMERIC(15, 2) NOT NULL,
    valor_troco NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    
    forma_pagamento VARCHAR(20) NOT NULL DEFAULT 'DINHEIRO', -- DINHEIRO, PIX, CARTAO_DEBITO, CARTAO_CREDITO
    
    -- Reforma Tributária 2026 (EC 132/23)
    valor_cbs NUMERIC(15, 2) NOT NULL DEFAULT 0.00, -- 0.9%
    valor_ibs NUMERIC(15, 2) NOT NULL DEFAULT 0.00, -- 0.1%
    
    -- Documento Fiscal NFC-e
    chave_nfce VARCHAR(44),
    status_venda VARCHAR(20) NOT NULL DEFAULT 'CONCLUIDA' -- CONCLUIDA, CANCELADA
);

CREATE INDEX IF NOT EXISTS idx_venda_pdv_data ON ponto_de_venda.venda_pdv(data_venda);

-- ------------------------------------------------------------------------------
-- 3. Itens da Venda Rápida
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ponto_de_venda.item_venda_pdv (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    venda_pdv_id BIGINT NOT NULL REFERENCES ponto_de_venda.venda_pdv(id) ON DELETE CASCADE,
    produto_id BIGINT NOT NULL REFERENCES comercial.produto(id) ON DELETE RESTRICT,
    numero_item INTEGER NOT NULL,
    
    quantidade NUMERIC(15, 4) NOT NULL DEFAULT 1.0000,
    preco_unitario NUMERIC(15, 4) NOT NULL,
    valor_total NUMERIC(15, 2) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_item_venda_pdv ON ponto_de_venda.item_venda_pdv(venda_pdv_id);
