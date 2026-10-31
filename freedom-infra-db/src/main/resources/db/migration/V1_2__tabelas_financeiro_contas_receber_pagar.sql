-- ==============================================================================
-- V1_2__tabelas_financeiro_contas_receber_pagar.sql
-- Freedom ERP - Títulos a Receber e Pagar no Schema financeiro
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- 1. FINANCEIRO: Contas a Receber
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS financeiro.conta_receber (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    filial_id BIGINT NOT NULL REFERENCES configuracao.filial(id) ON DELETE RESTRICT,
    cliente_id BIGINT NOT NULL REFERENCES comercial.pessoa(id) ON DELETE RESTRICT,
    numero_documento VARCHAR(30) NOT NULL,
    parcela INTEGER NOT NULL DEFAULT 1,
    total_parcelas INTEGER NOT NULL DEFAULT 1,
    
    data_emissao DATE NOT NULL,
    data_vencimento DATE NOT NULL,
    data_liquidacao DATE,

    valor_nominal NUMERIC(15, 2) NOT NULL,
    valor_desconto NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    valor_juros NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    valor_multa NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    valor_pago NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    saldo_devedor NUMERIC(15, 2) NOT NULL,

    status_titulo VARCHAR(20) NOT NULL DEFAULT 'PENDENTE', -- PENDENTE, QUITADO_PARCIAL, QUITADO, CANCELADO
    tipo_cobranca VARCHAR(20) NOT NULL DEFAULT 'BOLETO', -- BOLETO, PIX, CARTAO_CREDITO, DINHEIRO
    chave_transacao_pix VARCHAR(100),

    historico TEXT,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_conta_receber_vencimento ON financeiro.conta_receber(data_vencimento);
CREATE INDEX IF NOT EXISTS idx_conta_receber_cliente ON financeiro.conta_receber(cliente_id);
CREATE INDEX IF NOT EXISTS idx_conta_receber_status ON financeiro.conta_receber(status_titulo);

-- ------------------------------------------------------------------------------
-- 2. FINANCEIRO: Contas a Pagar
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS financeiro.conta_pagar (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    filial_id BIGINT NOT NULL REFERENCES configuracao.filial(id) ON DELETE RESTRICT,
    fornecedor_id BIGINT NOT NULL REFERENCES comercial.pessoa(id) ON DELETE RESTRICT,
    numero_documento VARCHAR(30) NOT NULL,
    parcela INTEGER NOT NULL DEFAULT 1,
    total_parcelas INTEGER NOT NULL DEFAULT 1,
    
    data_emissao DATE NOT NULL,
    data_vencimento DATE NOT NULL,
    data_pagamento DATE,

    valor_nominal NUMERIC(15, 2) NOT NULL,
    valor_desconto NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    valor_juros NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    valor_multa NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    valor_pago NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    saldo_devedor NUMERIC(15, 2) NOT NULL,

    status_titulo VARCHAR(20) NOT NULL DEFAULT 'PENDENTE', -- PENDENTE, PAGO_PARCIAL, PAGO, CANCELADO
    historico TEXT,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_conta_pagar_vencimento ON financeiro.conta_pagar(data_vencimento);
CREATE INDEX IF NOT EXISTS idx_conta_pagar_fornecedor ON financeiro.conta_pagar(fornecedor_id);
