# Análise de Sistemas & Especificações Formais (SDD / Spec Kit)

Este documento estabelece as especificações formais de casos de uso pelo método **SDD (Specification-Driven Development)**, fornecendo o **Spec Kit** do Freedom ERP.

---

## 1. Metodologia SDD e Spec Kit

No desenvolvimento orientado por especificações (SDD):
1. **Invariantes Inegociáveis**: Nenhuma linha de código é aceita sem atender previamente às pré-condições, pós-condições e cenários de teste *Given-When-Then*.
2. **Determinismo**: Todos os fluxos alternativos e de exceção são catalogados preventivamente.

---

## 2. Casos de Uso Críticos (Spec Kit)

### UC-01: Aprovação de Pedido de Venda e Reserva de Estoque

* **Ator Principal**: Vendedor / Gerente Comercial.
* **Pré-condições**:
  1. O pedido de venda deve estar no status `PEDIDO_PENDENTE`.
  2. O cliente associado deve estar no status `ATIVO` e sem bloqueio por inadimplência.
* **Pós-condições**:
  1. O status do pedido passa para `APROVADO`.
  2. A quantidade de cada item do pedido é adicionada ao `saldo_reservado` dos respectivos produtos.
  3. O `saldo_disponivel` é decrementado proporcionalmente.
  4. Um evento de auditoria `PEDIDO_APROVADO` é gravado no log.

#### Fluxo Principal:
1. O usuário aciona a ação de "Aprovar Pedido".
2. O sistema verifica a situação cadastral e financeira do cliente.
3. Para cada item do pedido, o sistema consulta se $Saldo\_Fisico - Saldo\_Reservado \ge Quantidade\_Item$.
4. Se todos os itens possuírem saldo suficiente, o sistema atualiza as reservas de estoque em transação única (`@Transactional`).
5. O sistema atualiza o status do pedido para `APROVADO`.
6. A interface atualiza o status visual para a cor verde e exibe notificação Toast: *"Pedido aprovado e estoque reservado com sucesso"*.

#### Fluxos Alternativos e Exceções:
* **Exceção 3a: Cliente com Títulos Vencidos (Inadimplente)**:
  * O sistema interrompe o fluxo e exibe modal: *"Cliente possui títulos vencidos há mais de 5 dias. Aprovação requer permissão de Gerente Financeiro"*.
* **Exceção 3b: Saldo Insuficiente em Estoque**:
  * O sistema lista na tela os itens com saldo insuficiente com a indicação da quantidade faltante.
  * O pedido permanece como `PEDIDO_PENDENTE` e o sistema oferece a opção de gerar Ordem de Produção (PCP) ou Solicitação de Compras (GMS).

#### Cenário de Teste BDD (Spec Kit):
```gherkin
Cenário: Aprovar pedido com estoque suficiente e cliente regular
  Dado que o cliente "Empresa ABC" está ativo e sem títulos vencidos
  E o produto "Cabo de Rede Cat6" possui 100 metros de saldo físico e 10 metros reservados
  Quando o vendedor aprova o pedido contendo 30 metros de "Cabo de Rede Cat6"
  Então o status do pedido deve ser alterado para "APROVADO"
  E o saldo reservado do produto deve ser atualizado para 40 metros
  E o saldo disponível do produto deve ser de 60 metros
```

---

### UC-02: Faturamento de Venda e Emissão de NF-e (Layout 2026 com CBS e IBS)

* **Ator Principal**: Faturista / Operador Fiscal.
* **Pré-condições**:
  1. O pedido deve estar no status `APROVADO`.
  2. A empresa emissora deve possuir Certificado Digital A1 válido configurado.
* **Pós-condições**:
  1. Geração do XML da NF-e assinado digitalmente.
  2. Transmissão para a SEFAZ do estado e recebimento do protocolo de autorização (`cStat = 100`).
  3. Status do pedido atualizado para `FATURADO`.
  4. Baixa física definitiva do estoque (conversão de saldo reservado para saída efetiva).
  5. Geração dos títulos a receber no módulo Financeiro (`financeiro_contas_receber`).
  6. Envio automático do DANFE em PDF e XML por WhatsApp e e-mail para o cliente.

#### Fluxo Principal:
1. O faturista seleciona o pedido aprovado e aciona "Emitir NF-e".
2. O motor tributário calcula as bases e alíquotas:
   * ICMS, IPI, PIS, COFINS (conforme regras vigentes do estado).
   * **CBS (0,9%)** e **IBS (0,1%)** conforme a EC 132/2023.
   * Valor aproximado dos tributos pelo IBPT.
3. O sistema monta o XML no schema oficial, assina digitalmente com chave privada RSA do certificado A1 e transmite via HTTPS.
4. A SEFAZ retorna `cStat = 100 - Autorizado o uso da NF-e`.
5. Em transação atômica no PostgreSQL:
   * Grava o protocolo de autorização e chave de acesso de 44 dígitos.
   * Executa a saída física do estoque.
   * Gera as parcelas no Contas a Receber.
6. A impressora padrão emite o DANFE e a mensagem de sucesso é apresentada.

---

### UC-03: Baixa de Título no Contas a Receber com Juros e Descontos

* **Ator Principal**: Operador de Caixa / Analista de Contas a Receber.
* **Pré-condições**:
  1. O título deve existir no banco com status `ABERTO` ou `PARCIALMENTE_PAGO`.
* **Pós-condições**:
  1. Se $Valor\_Pago \ge Saldo\_Devedor$, status passa para `QUITADO`.
  2. Se $Valor\_Pago < Saldo\_Devedor$, status passa para `PARCIALMENTE_PAGO` com o novo saldo registrado.
  3. Lançamento da entrada no extrato da conta bancária de destino.

#### Cenário de Teste BDD (Spec Kit):
```gherkin
Cenário: Baixa integral de título vencido com cálculo exato de multa e juros
  Dado que existe uma fatura de R$ 1.000,00 vencida há 15 dias
  E a regra estipula 2% de multa fixa e 1% de juros ao mês pro-rata dia
  Quando o operador registra o pagamento integral da fatura
  Então o sistema deve calcular exatamente R$ 20,00 de multa
  E o sistema deve calcular exatamente R$ 5,00 de juros (15 dias)
  E o valor total a ser recebido deve ser de R$ 1.025,00
  E o status da fatura deve ser atualizado para "QUITADO"
```

