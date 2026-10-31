---
name: erp-module-specs
description: >-
  Catálogo canônico e especificações de regras de negócio, tabelas, dependências
  e fluxos de cada módulo do Freedom ERP (STD, FNC, GMS, PCP, LVF, CRM, ATD, PDV, CFG, REP).
---

# Especificações dos Módulos do Freedom ERP

Este skill documenta as fronteiras funcionais, regras de validação e modelo operacional de cada módulo do sistema.

## 1. Módulo STD (Standard — Cadastros Centrais & Vendas)

### Entidades Centrais:
* **Cliente / Fornecedor / Transportadora**: Unificação na entidade `Pessoa` com papéis atribuídos (Cliente, Fornecedor, Representante/Vendedor).
* **Produto & Serviço**: NCM, unidade de medida, código de barras EAN-13, peso líquido/bruto, alíquotas tributárias e composição.
* **Tabela de Preço**: Preço base com indexadores, margem de markup e regras de vigência.
* **Pedido de Venda / Orçamento**:
  * Ciclo de Vida: `ORCAMENTO -> PEDIDO_PENDENTE -> APROVADO -> FATURADO -> ENTREGUE / CANCELADO`.
  * Regras de Validação: Verificação de limite de crédito do cliente, bloqueio de inadimplentes e reserva de saldo de estoque.

---

## 2. Módulo FNC (Financeiro)

### Subdomínios:
* **Contas a Receber (`Fnreceber`)**: Originado de vendas ou lançamentos manuais.
  * Baixa parcial, quitação integral, cálculo automático de juros simples/compostos, multa contratual e descontos de pontualidade.
* **Contas a Pagar (`Fnpagar`)**: Originado de compras de mercadorias ou despesas operacionais.
  * Fluxo de aprovação de pagamentos por alçada.
* **Tesouraria & Bancos**: Extratos de contas correntes, transferências entre contas e conciliação bancária via OFX / API Open Finance.
* **Cobrança Eletrônica**: Geração de Boletos Registrados e PIX Dinâmico com Webhook de confirmação instantânea de pagamento.

---

## 3. Módulo GMS (Gestão de Materiais e Suprimentos / Estoque & Compras)

### Regras Centrais:
* **Controle de Lotes e Validade**: Rastreabilidade obrigatória para produtos químicos, alimentos e medicamentos.
* **Custo Médio Ponderado**: Recalculado automaticamente a cada entrada por compra de mercadoria:
  $$Custo\_Medio_{novo} = \frac{(Estoque_{atual} \times Custo_{atual}) + (Qtd_{entrada} \times Valor\_Custo_{entrada})}{Estoque_{atual} + Qtd_{entrada}}$$
* **Almoxarifados Múltiplos**: Movimentações internas com documento de transferência e aprovação.
* **Requisição e Pedido de Compra**: Cotação com múltiplos fornecedores e aprovação por alçada.

---

## 4. Módulo PCP (Planejamento e Controle de Produção)

### Regras de Produção:
* **Lista de Materiais (BOM - Bill of Materials)**: Estrutura em árvore de produtos finais, componentes e matérias-primas.
* **Ordem de Produção (OP)**:
  * Empenho automático de matéria-prima no estoque ao liberar a OP.
  * Apontamento de horas de mão de obra e consumo de insumos por fase de operação.
  * Entrada do produto acabado no almoxarifado com cálculo do custo de fabricação.

---

## 5. Módulo LVF (Livros Fiscais & SPED)

* **Apuração de Impostos**: Livro Registro de Entradas (Mod. 1/1A), Livro de Saídas (Mod. 2) e Livro de Apuração do ICMS.
* **Geração de Arquivos Fiscais**: EFD ICMS/IPI (SPED Fiscal) e EFD Contribuições (PIS/COFINS).

---

## 6. Módulo PDV (Ponto de Venda)

* Abertura de Caixa, Controle de Turno de Operadores.
* Venda Rápida de Balcão com leitor de código de barras.
* Sangria (retirada de dinheiro) e Suprimento (reforço de troco).
* Emissão de NFC-e com contingência e impressão térmica.
* Fechamento cego de caixa com relatório de quebra de caixa.

