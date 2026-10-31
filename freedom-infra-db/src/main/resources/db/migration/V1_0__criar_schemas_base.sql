-- ==============================================================================
-- V1_0__criar_schemas_base.sql
-- Freedom ERP - Inicialização de Schemas de Negócio no PostgreSQL 16+
-- ==============================================================================

-- 1. Criação das Extensões Essenciais
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm"; -- Suporte a buscas parciais de texto de alta performance (substitui CONTAINING do Firebird)
CREATE EXTENSION IF NOT EXISTS "unaccent"; -- Suporte a buscas sem considerar acentuação

-- 2. Criação dos Schemas Departamentais (Desacoplamento de Domínios)
CREATE SCHEMA IF NOT EXISTS cfg; -- Configurações Centrais, Segurança e Licenciamento
CREATE SCHEMA IF NOT EXISTS std; -- Pessoas, Produtos, Tabela de Preços e Vendas
CREATE SCHEMA IF NOT EXISTS fnc; -- Financeiro (Contas a Receber/Pagar, Bancos, PIX)
CREATE SCHEMA IF NOT EXISTS gms; -- Gestão de Materiais, Estoque e Compras
CREATE SCHEMA IF NOT EXISTS pcp; -- Planejamento e Controle de Produção
CREATE SCHEMA IF NOT EXISTS lvf; -- Livros Fiscais e Regras Tributárias
CREATE SCHEMA IF NOT EXISTS pdv; -- Ponto de Venda e Operações de Frente de Caixa

COMMENT ON SCHEMA cfg IS 'Configurações de sistema, usuários, filiais e parâmetros';
COMMENT ON SCHEMA std IS 'Módulo Standard: cadastros centrais e pedidos de venda';
COMMENT ON SCHEMA fnc IS 'Módulo Financeiro: tesouraria, títulos e conciliação';
COMMENT ON SCHEMA gms IS 'Módulo de Materiais: almoxarifados, lotes e compras';
COMMENT ON SCHEMA pcp IS 'Módulo de Produção: ficha técnica e ordens de produção';
COMMENT ON SCHEMA lvf IS 'Módulo Fiscal: apuração, EFD e transição 2026';
COMMENT ON SCHEMA pdv IS 'Módulo Frente de Caixa: vendas rápidas e NFC-e';

