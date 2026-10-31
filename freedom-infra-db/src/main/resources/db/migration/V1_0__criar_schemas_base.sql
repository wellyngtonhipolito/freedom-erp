-- ==============================================================================
-- V1_0__criar_schemas_base.sql
-- Freedom ERP - Inicialização de Schemas Legíveis de Negócio no PostgreSQL 16+
-- Nomes explícitos substituindo siglas crípticas legadas
-- ==============================================================================

-- 1. Extensões Essenciais
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
CREATE EXTENSION IF NOT EXISTS "unaccent";

-- 2. Schemas Departamentais Canônicos e Expressivos
CREATE SCHEMA IF NOT EXISTS configuracao;    -- Antigo CFG: Configurações, Segurança e Parâmetros
CREATE SCHEMA IF NOT EXISTS comercial;       -- Antigo STD: Pessoas, Clientes, Produtos e Pedidos
CREATE SCHEMA IF NOT EXISTS financeiro;      -- Antigo FNC: Contas a Receber/Pagar, Tesouraria e PIX
CREATE SCHEMA IF NOT EXISTS estoque_compras; -- Antigo GMS: Estoque, Lotes, Almoxarifado e Compras
CREATE SCHEMA IF NOT EXISTS producao;        -- Antigo PCP: Ordens de Produção e Estrutura de Produtos
CREATE SCHEMA IF NOT EXISTS fiscal;          -- Antigo LVF: Apuração Fiscal, Livros e Reforma 2026
CREATE SCHEMA IF NOT EXISTS ponto_de_venda;  -- Antigo PDV: Frente de Caixa e NFC-e

COMMENT ON SCHEMA configuracao IS 'Configurações de sistema, usuários, empresas e filiais';
COMMENT ON SCHEMA comercial IS 'Módulo Comercial: cadastros centrais, clientes, produtos e vendas';
COMMENT ON SCHEMA financeiro IS 'Módulo Financeiro: tesouraria, contas a pagar/receber e conciliação';
COMMENT ON SCHEMA estoque_compras IS 'Módulo de Estoque e Compras: suprimentos, almoxarifados e cotações';
COMMENT ON SCHEMA producao IS 'Módulo de Produção: ficha técnica e ordens de produção industrial';
COMMENT ON SCHEMA fiscal IS 'Módulo Fiscal: apuração, EFD e transição da Reforma Tributária 2026';
COMMENT ON SCHEMA ponto_de_venda IS 'Módulo Frente de Caixa: vendas rápidas de balcão e NFC-e';
