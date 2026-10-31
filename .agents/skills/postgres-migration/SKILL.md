---
name: postgres-migration
description: >-
  Diretrizes estritas de migração de banco de dados do Firebird 1.5/2.5 para PostgreSQL 16+.
  Cobre conversão de tipos de dados, eliminação de UDFs em C, Sequences, IDENTITY columns,
  triggers PL/pgSQL, índices, otimização e scripts Flyway versionados.
---

# Guia de Migração: Firebird para PostgreSQL 16+

Este skill contém o guia definitivo para conversão de schemas, estruturas e rotinas de banco de dados do Freedom ERP de **Firebird** para **PostgreSQL 16+**.

## 1. Mapeamento de Tipos de Dados

| Firebird 1.5 / 2.5 | PostgreSQL 16+ | Justificativa / Regra |
| :--- | :--- | :--- |
| `SMALLINT` | `SMALLINT` | Inteiro de 2 bytes (-32768 a 32767). |
| `INTEGER` | `INTEGER` | Inteiro de 4 bytes (-2.147.483.648 a 2.147.483.647). |
| `BIGINT` | `BIGINT` | Inteiro de 8 bytes (usado em chaves primárias e sequenciais). |
| `NUMERIC(p, s)` | `NUMERIC(p, s)` | Valores monetários exatos (ex: `NUMERIC(15, 2)` para valores, `NUMERIC(15, 4)` para quantidades). |
| `VARCHAR(n)` | `VARCHAR(n)` | Cadeia de caracteres com limite. |
| `CHAR(1)` | `CHAR(1)` ou `BOOLEAN` | Para flags ('S'/'N') converter para `BOOLEAN` (`true`/`false`). |
| `DATE` | `DATE` | Apenas data (sem hora). |
| `TIME` | `TIME` | Apenas hora. |
| `TIMESTAMP` | `TIMESTAMPTZ` | Data e hora com fuso horário (UTC recomendado). |
| `BLOB SUB_TYPE 1` | `TEXT` | Textos longos, observações e logs. |
| `BLOB SUB_TYPE 0` | `BYTEA` | Binários, fotos e assinaturas digitais. |

---

## 2. Substituição de Mecanismos Proprietários do Firebird

### 2.1. Generators para Sequences e IDENTITY
* **Firebird (Legado):**
  ```sql
  CREATE GENERATOR GEN_CODCLI;
  SET GENERATOR GEN_CODCLI TO 100;
  -- Uso em trigger com GEN_ID(GEN_CODCLI, 1)
  ```
* **PostgreSQL (Moderno):**
  ```sql
  -- Preferência 1: IDENTITY Column (Padrão ANSI)
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
  
  -- Ou Sequence tradicional vinculada:
  CREATE SEQUENCE seq_cliente_id START WITH 1 INCREMENT BY 1;
  ```

### 2.2. Eliminação de UDFs em C
O Firebird exigia carregar arquivos de biblioteca C (`ib_udf.so`, `fbudf.dll`) para operações básicas. No PostgreSQL, todas são funções nativas:
* `ABS(x)` $\rightarrow$ `abs(x)` nativo.
* `ADDDAY(d, n)` $\rightarrow$ `d + (n * INTERVAL '1 day')`.
* `ADDMONTH(d, n)` $\rightarrow$ `d + (n * INTERVAL '1 month')`.
* `SUBSTRING(s, ini, tam)` $\rightarrow$ `substring(s from ini for tam)` nativo.

### 2.3. Sintaxe de Queries e Paginação
* `FIRST 20 SKIP 40` $\rightarrow$ `LIMIT 20 OFFSET 40`.
* `CONTAINING` $\rightarrow$ `ILIKE '%termo%'` (ou índice GiST/GIN com extensão `pg_trgm` para alta performance).
* Tratamento de NULL: no PostgreSQL, `NULLS FIRST` e `NULLS LAST` funcionam de forma explícita na cláusula `ORDER BY`.

---

## 3. Padrão de Versionamento de Schema com Flyway

Todo script deve seguir o padrão:
`src/main/resources/db/migration/V{Versao}__{Descricao}.sql`

Exemplo:
* `V1_0__criar_schemas_base.sql`
* `V1_1__tabelas_cadastros_principais.sql`
* `V1_2__tabelas_financeiro_contas_receber_pagar.sql`
* `V1_3__tabelas_vendas_e_faturamento.sql`

