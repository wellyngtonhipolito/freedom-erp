# Arquitetura de Dados & Especificação PostgreSQL 16+

Este documento define a arquitetura do banco de dados relacional do **Freedom ERP**, estabelecendo as normas técnicas para a migração e operação no **PostgreSQL 16+**.

---

## 1. Convenções Estritas de Nomenclatura e Tipagem

Para garantir consistência e eliminar ambiguidades:
* **Tabelas**: Nomes no formato `{modulo}_{entidade_plural}` em minúsculas com sublinhado (`snake_case`).
  * Exemplo: `vendas_clientes`, `vendas_pedidos`, `financeiro_contas_receber`, `estoque_produtos`.
* **Colunas**: Formato `{nome_atributo}` em minúsculas com sublinhado.
  * Chaves Primárias: Sempre `id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY`.
  * Chaves Estrangeiras: `{entidade_singular}_id BIGINT NOT NULL REFERENCES {tabela}(id)`.
  * Valores Monetários: `NUMERIC(15, 2) NOT NULL DEFAULT 0.00`.
  * Quantidades e Unidades: `NUMERIC(15, 4) NOT NULL DEFAULT 0.0000`.
  * Flags e Booleanos: Tipo nativo `BOOLEAN NOT NULL DEFAULT true`.
  * Timestamps: `TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP`.

---

## 2. Estrutura do DDL Canônico (Exemplo: Módulo de Clientes e Vendas)

```sql
-- Schema de Cadastros Base e Vendas
CREATE SCHEMA IF NOT EXISTS vendas;

-- Tabela de Pessoas / Clientes
CREATE TABLE IF NOT EXISTS vendas.clientes (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    filial_id BIGINT NOT NULL,
    tipo_pessoa CHAR(1) NOT NULL CHECK (tipo_pessoa IN ('F', 'J')), -- F=Física, J=Jurídica
    cpf_cnpj VARCHAR(14) NOT NULL,
    razao_social VARCHAR(150) NOT NULL,
    nome_fantasia VARCHAR(150),
    inscricao_estadual VARCHAR(20),
    inscricao_municipal VARCHAR(20),
    cep VARCHAR(8) NOT NULL,
    logradouro VARCHAR(150) NOT NULL,
    numero VARCHAR(20) NOT NULL,
    complemento VARCHAR(50),
    bairro VARCHAR(100) NOT NULL,
    cidade_ibge_id VARCHAR(7) NOT NULL,
    cidade_nome VARCHAR(100) NOT NULL,
    uf CHAR(2) NOT NULL,
    telefone VARCHAR(15),
    celular VARCHAR(15),
    email VARCHAR(150),
    limite_credito NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    situacao CHAR(1) NOT NULL DEFAULT 'A' CHECK (situacao IN ('A', 'I', 'B')), -- Ativo, Inativo, Bloqueado
    observacoes TEXT,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uq_clientes_empresa_documento UNIQUE (empresa_id, cpf_cnpj)
);

-- Índices de Alta Performance
CREATE INDEX idx_clientes_empresa_filial ON vendas.clientes (empresa_id, filial_id);
CREATE INDEX idx_clientes_cpf_cnpj ON vendas.clientes (cpf_cnpj);
CREATE INDEX idx_clientes_razao_social_trgm ON vendas.clientes USING gin (razao_social gin_trgm_ops);

-- Tabela de Pedidos de Venda
CREATE TABLE IF NOT EXISTS vendas.pedidos (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    filial_id BIGINT NOT NULL,
    numero_pedido BIGINT NOT NULL,
    cliente_id BIGINT NOT NULL REFERENCES vendas.clientes(id),
    vendedor_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ORCAMENTO' CHECK (status IN (
        'ORCAMENTO', 'PEDIDO_PENDENTE', 'APROVADO', 'REPROVADO', 
        'EM_SEPARACAO', 'FATURADO', 'ENTREGUE', 'CANCELADO'
    )),
    data_emissao TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_aprovacao TIMESTAMPTZ,
    data_faturamento TIMESTAMPTZ,
    data_cancelamento TIMESTAMPTZ,
    valor_produtos NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    valor_desconto NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    valor_frete NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    valor_seguro NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    valor_outras_despesas NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    valor_total NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    condicao_pagamento_id BIGINT NOT NULL,
    natureza_operacao_id BIGINT NOT NULL,
    motivo_cancelamento TEXT,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_pedidos_empresa_numero UNIQUE (empresa_id, filial_id, numero_pedido)
);

-- Itens do Pedido de Venda
CREATE TABLE IF NOT EXISTS vendas.pedido_itens (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pedido_id BIGINT NOT NULL REFERENCES vendas.pedidos(id) ON DELETE CASCADE,
    numero_item INT NOT NULL,
    produto_id BIGINT NOT NULL,
    cfop VARCHAR(4) NOT NULL,
    cst_icms VARCHAR(3),
    csosn VARCHAR(3),
    aliquota_icms NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    aliquota_ipi NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    aliquota_pis NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    aliquota_cofins NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    aliquota_cbs NUMERIC(5, 2) NOT NULL DEFAULT 0.00, -- Reforma Tributária 2026
    aliquota_ibs NUMERIC(5, 2) NOT NULL DEFAULT 0.00, -- Reforma Tributária 2026
    quantidade NUMERIC(15, 4) NOT NULL,
    preco_unitario NUMERIC(15, 4) NOT NULL,
    valor_desconto NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    valor_total NUMERIC(15, 2) NOT NULL,

    CONSTRAINT uq_pedido_item_seq UNIQUE (pedido_id, numero_item)
);

CREATE INDEX idx_pedido_itens_pedido_id ON vendas.pedido_itens (pedido_id);
```

---

## 3. Triggers em PL/pgSQL Substituindo Funções Legadas

No Firebird legado, existiam triggers complexas e UDFs em C para controle de datas e sequenciais. No PostgreSQL, criamos triggers limpas em PL/pgSQL:

```sql
-- Função genérica de atualização de timestamp
CREATE OR REPLACE FUNCTION public.fn_atualiza_timestamp_modificacao()
RETURNS TRIGGER AS $$
BEGIN
    NEW.atualizado_em = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger aplicada nas tabelas
CREATE TRIGGER trg_clientes_atualizado_em
    BEFORE UPDATE ON vendas.clientes
    FOR EACH ROW
    EXECUTE FUNCTION public.fn_atualiza_timestamp_modificacao();
```

---

## 4. Estratégia de Mapeamento com JPA 3.2 e Hibernate 6.6+

### 4.1. Mapeamento das Entidades
1. **Identificadores Simples**: Uso de `@GeneratedValue(strategy = GenerationType.IDENTITY)`.
2. **Datas**: Uso exclusivo de `java.time.LocalDate` e `java.time.Instant`.
3. **Lazy Loading Padrão**: Todo relacionamento `@ManyToOne` e `@OneToMany` deve ser explicitamente anotado com `fetch = FetchType.LAZY` para evitar carregar grafos gigantescos de objetos desnecessariamente.
4. **Auditoria com Envers**: Manutenção de histórico completo de alterações com `@Audited` do Hibernate Envers em cadastros críticos.

