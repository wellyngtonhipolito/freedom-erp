-- ==============================================================================
-- V1_4__pedidos_venda_completo.sql
-- Freedom ERP - Módulo Comercial: Pedidos de Venda, Itens e Integração Financeira
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- 1. Cabeçalho de Pedido de Venda / Orçamento
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS comercial.pedido_venda (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    filial_id BIGINT NOT NULL REFERENCES configuracao.filial(id) ON DELETE RESTRICT,
    cliente_id BIGINT NOT NULL REFERENCES comercial.pessoa(id) ON DELETE RESTRICT,
    numero_pedido VARCHAR(30) NOT NULL,
    data_emissao DATE NOT NULL DEFAULT CURRENT_DATE,
    data_previsao_entrega DATE,
    
    -- Valores Totais
    valor_itens NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    valor_frete NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    valor_desconto NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    valor_total NUMERIC(15, 2) NOT NULL DEFAULT 0.00,

    -- Totais Tributários Reforma 2026 (EC 132/23)
    valor_icms NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    valor_cbs NUMERIC(15, 2) NOT NULL DEFAULT 0.00,  -- Federal (0.9% teste 2026)
    valor_ibs NUMERIC(15, 2) NOT NULL DEFAULT 0.00,  -- Estadual/Municipal (0.1% teste 2026)
    valor_imposto_seletivo NUMERIC(15, 2) NOT NULL DEFAULT 0.00,

    -- Ciclo de Vida: ORCAMENTO -> APROVADO -> FATURADO -> CANCELADO
    status_pedido VARCHAR(20) NOT NULL DEFAULT 'ORCAMENTO',
    forma_pagamento VARCHAR(20) NOT NULL DEFAULT 'PIX',
    observacao TEXT,

    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_pedido_cliente ON comercial.pedido_venda(cliente_id);
CREATE INDEX IF NOT EXISTS idx_pedido_status ON comercial.pedido_venda(status_pedido);
CREATE INDEX IF NOT EXISTS idx_pedido_data ON comercial.pedido_venda(data_emissao);

-- ------------------------------------------------------------------------------
-- 2. Itens do Pedido de Venda
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS comercial.item_pedido_venda (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pedido_venda_id BIGINT NOT NULL REFERENCES comercial.pedido_venda(id) ON DELETE CASCADE,
    produto_id BIGINT NOT NULL REFERENCES comercial.produto(id) ON DELETE RESTRICT,
    numero_item INTEGER NOT NULL,
    
    quantidade NUMERIC(15, 4) NOT NULL DEFAULT 1.0000,
    preco_unitario NUMERIC(15, 4) NOT NULL,
    valor_desconto NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    valor_total NUMERIC(15, 2) NOT NULL,

    -- Tributos por Item
    aliquota_icms NUMERIC(5, 2) NOT NULL DEFAULT 18.00,
    valor_icms NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    aliquota_cbs NUMERIC(5, 2) NOT NULL DEFAULT 0.90, -- 0.9%
    valor_cbs NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    aliquota_ibs NUMERIC(5, 2) NOT NULL DEFAULT 0.10, -- 0.1%
    valor_ibs NUMERIC(15, 2) NOT NULL DEFAULT 0.00,

    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_item_pedido_venda ON comercial.item_pedido_venda(pedido_venda_id);

