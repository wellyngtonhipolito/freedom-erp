---
name: clean-architecture-guardrails
description: >-
  Guardrails e regras de arquitetura limpa, TDD, SDD e verificação estática com ArchUnit.
  Sem margem para interpretações: isolamento de camadas, imutabilidade de Records,
  padrões de teste e convenções de nomenclatura.
---

# Guardrails Arquiteturais & Qualidade (TDD / SDD)

Este skill define as regras inquebráveis de arquitetura, qualidade de código e testes automatizados para o Freedom ERP.

## 1. Guardrails Arquiteturais Estritos (ArchUnit)

As seguintes regras são verificadas automaticamente no build via testes de unidade do **ArchUnit**:

1. **Camada de Apresentação (Swing)**:
   * **PROIBIÇÃO TOTAL** de importação de classes de `java.sql.*`, `javax.sql.*`, `jakarta.persistence.*`, `org.hibernate.*`.
   * Telas só podem se comunicar com interfaces no pacote `*.service.*` ou `*.controller.*`.
2. **Camada de Domínio**:
   * O pacote `*.domain.*` é puro e independente. Não deve importar nenhum pacote de interface gráfica (`java.awt.*`, `javax.swing.*`, `com.formdev.flatlaf.*`).
3. **Imutabilidade de DTOs**:
   * Toda transferência de dados entre serviços e telas deve utilizar **Java Records** ou DTOs com propriedades finais imutáveis.
4. **Ciclo de Dependências Proibido**:
   * É terminantemente proibida dependência cíclica entre submódulos funcionais (ex: o Financeiro pode consultar Vendas, mas Vendas não deve depender diretamente de classes de implementação do Financeiro).

---

## 2. Test-Driven Development (TDD) Mandatório

Qualquer nova funcionalidade ou refatoração deve obrigatoriamente seguir o ciclo TDD:
1. **Red**: Escrever o teste unitário antes de escrever o código de produção, cobrindo o comportamento esperado e as regras de borda.
2. **Green**: Escrever a implementação mínima necessária para o teste passar.
3. **Refactor**: Limpar o código, eliminar duplicidades e assegurar que as convenções de nomenclatura foram respeitadas.

### 2.1. Pirâmide de Testes
* **Testes Unitários (70%+)**: JUnit 5 + Mockito para regras de cálculo fiscal, comissões, juros, validação de CPF/CNPJ e transições de estados.
* **Testes de Integração (20%)**: Testcontainers com PostgreSQL real para validar queries JPA, transações e migrations Flyway.
* **Testes de UI/E2E (10%)**: AssertJ-Swing para validação de fluxos críticos de telas (abertura de pedido, consulta de cliente e fechamento de caixa).

---

## 3. Padrão de Nomenclatura Estrito (Sem Brechas)

* **Classes de Domínio**: Nomes substantivos singulares em português ou inglês corporativo (`Cliente`, `PedidoVenda`, `ItemPedido`, `ContaReceber`, `LancamentoEstoque`).
* **Interfaces e Implementações**: Repositórios como `ClienteRepository`, serviços como `ClienteService` (interface) e `ClienteServiceImpl` (se aplicável).
* **Colunas de Banco de Dados**: Formato `snake_case` minúsculo e descritivo (`cliente_id`, `razao_social`, `data_cadastro`, `valor_liquido`).
* **Métodos**: Verbos no infinitivo em camelCase (`calcularValorTotal()`, `aprovarPedido()`, `estornarLancamento()`).

