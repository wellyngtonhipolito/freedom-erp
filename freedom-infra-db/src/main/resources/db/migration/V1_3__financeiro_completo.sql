-- ==============================================================================
-- V1_3__financeiro_completo.sql
-- Freedom ERP - Módulo Financeiro Completo:
-- 1. Bancos e Contas Correntes
-- 2. Movimentação Bancária e Extrato de Caixa
-- 3. Histórico de Baixas de Contas a Receber (Recebimentos)
-- 4. Histórico de Baixas de Contas a Pagar (Pagamentos)
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- 1. Bancos Febraban
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS financeiro.banco (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo_febraban VARCHAR(3) NOT NULL UNIQUE,
    nome_banco VARCHAR(80) NOT NULL,
    site VARCHAR(100),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Carga inicial dos principais bancos brasileiros
INSERT INTO financeiro.banco (codigo_febraban, nome_banco) VALUES
('001', 'Banco do Brasil S.A.'),
('033', 'Banco Santander (Brasil) S.A.'),
('104', 'Caixa Econômica Federal'),
('237', 'Banco Bradesco S.A.'),
('260', 'Nu Pagamentos S.A. (Nubank)'),
('077', 'Banco Inter S.A.'),
('341', 'Itaú Unibanco S.A.')
ON CONFLICT (codigo_febraban) DO NOTHING;

-- ------------------------------------------------------------------------------
-- 2. Contas Bancárias / Tesouraria
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS financeiro.conta_bancaria (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    filial_id BIGINT NOT NULL REFERENCES configuracao.filial(id) ON DELETE RESTRICT,
    banco_id BIGINT NOT NULL REFERENCES financeiro.banco(id) ON DELETE RESTRICT,
    descricao VARCHAR(60) NOT NULL, -- Ex: 'Conta Principal Bradesco', 'Caixa Gaveta'
    agencia VARCHAR(10) NOT NULL,
    digito_agencia VARCHAR(2),
    numero_conta VARCHAR(20) NOT NULL,
    digito_conta VARCHAR(2),
    tipo_conta VARCHAR(20) NOT NULL DEFAULT 'CORRENTE', -- CORRENTE, POUPANCA, CAIXA_FISICO, APLICACAO
    saldo_atual NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    chave_pix VARCHAR(100),
    tipo_chave_pix VARCHAR(20), -- CNPJ, EMAIL, TELEFONE, ALEATORIA
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------------------------
-- 3. Movimentações Bancárias (Extrato da Conta)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS financeiro.movimento_bancario (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    conta_bancaria_id BIGINT NOT NULL REFERENCES financeiro.conta_bancaria(id) ON DELETE RESTRICT,
    data_movimento DATE NOT NULL,
    tipo_movimento VARCHAR(10) NOT NULL, -- 'CREDITO' ou 'DEBITO'
    valor NUMERIC(15, 2) NOT NULL,
    saldo_apos_movimento NUMERIC(15, 2) NOT NULL,
    origem_movimento VARCHAR(30) NOT NULL DEFAULT 'MANUAL', -- RECEBIMENTO_CLIENTE, PAGAMENTO_FORNECEDOR, TRANSFERENCIA, MANUAL
    documento_referencia VARCHAR(40),
    historico TEXT,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_mov_banco_conta_data ON financeiro.movimento_bancario(conta_bancaria_id, data_movimento);

-- ------------------------------------------------------------------------------
-- 4. Baixas e Liquidações de Contas a Receber (Recebimentos)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS financeiro.baixa_conta_receber (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    conta_receber_id BIGINT NOT NULL REFERENCES financeiro.conta_receber(id) ON DELETE CASCADE,
    conta_bancaria_id BIGINT REFERENCES financeiro.conta_bancaria(id) ON DELETE RESTRICT,
    data_baixa DATE NOT NULL,
    valor_pago NUMERIC(15, 2) NOT NULL,
    valor_juros NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    valor_multa NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    valor_desconto NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    forma_pagamento VARCHAR(20) NOT NULL DEFAULT 'PIX', -- PIX, BOLETO, TRANSFERENCIA, CARTAO, DINHEIRO
    comprovante_transacao VARCHAR(100),
    observacao TEXT,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_baixa_rec_titulo ON financeiro.baixa_conta_receber(conta_receber_id);

-- ------------------------------------------------------------------------------
-- 5. Baixas e Liquidações de Contas a Pagar (Pagamentos)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS financeiro.baixa_conta_pagar (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    conta_pagar_id BIGINT NOT NULL REFERENCES financeiro.conta_pagar(id) ON DELETE CASCADE,
    conta_bancaria_id BIGINT REFERENCES financeiro.conta_bancaria(id) ON DELETE RESTRICT,
    data_baixa DATE NOT NULL,
    valor_pago NUMERIC(15, 2) NOT NULL,
    valor_juros NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    valor_multa NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    valor_desconto NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    forma_pagamento VARCHAR(20) NOT NULL DEFAULT 'PIX',
    comprovante_transacao VARCHAR(100),
    observacao TEXT,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_baixa_pag_titulo ON financeiro.baixa_conta_pagar(conta_pagar_id);

