---
name: brazilian-tax-2026
description: >-
  Especialista em Legislação Tributária, Fiscal e Comercial Brasileira com vigência para 2026+.
  Cobre Reforma Tributária (EC 132/2023 - IBS, CBS, Imposto Seletivo), fase de transição com
  ICMS/ISS/PIS/COFINS, NF-e 4.00/5.00, NFC-e, EFD-Reinf, SPED Fiscal, cálculo trifásico,
  partilha de impostos, alíquotas interestaduais (DIFAL), Lei da Transparência (IBPT) e LGPD fiscal.
---

# Guia Especialista: Legislação Tributária e Fiscal Brasileira (2026+)

Este skill orienta a implementação e conformidade de todas as regras fiscais, tributárias e faturamento no Freedom ERP para o exercício de **2026 em diante**.

## 1. O Marco de 2026: A Reforma Tributária (Emenda Constitucional 132/2023)

O ano de **2026 é o marco zero da transição prática da Reforma Tributária sobre o Consumo**:
* **CBS (Contribuição sobre Bens e Serviços - Federal)**: Alíquota de teste/transição de 0,9%.
* **IBS (Imposto sobre Bens e Serviços - Estados e Municípios)**: Alíquota de teste/transição de 0,1%.
* **Total de 1%** compensável com a apuração de PIS e COFINS no mesmo período.
* **Imposto Seletivo (IS)**: Tributação incidente sobre produtos prejudiciais à saúde ou ao meio ambiente (veículos poluentes, cigarros, bebidas alcoólicas, etc.).

### 1.1. Coexistência Obrigatória do Sistema Híbrido
O ERP **DEVE** suportar simultaneamente:
1. O modelo tributário tradicional vigente:
   * **ICMS** (com regras de Substituição Tributária - ICMS-ST e Diferencial de Alíquota - DIFAL).
   * **ISS** (Imposto Sobre Serviços de Qualquer Natureza para notas de serviço).
   * **PIS** e **COFINS** (regimes cumulativo e não-cumulativo).
   * **IPI** (Imposto sobre Produtos Industrializados).
2. O novo modelo da Reforma:
   * Campos e registros para apuração e destaque de **CBS**, **IBS** e **IS**.
   * Princípio da não-cumulatividade plena (crédito financeiro de todas as aquisições tributadas).
   * Tributação no **destino** (local de consumo do bem ou serviço) e não na origem.

---

## 2. Documentos Fiscais Eletrônicos (DF-e)

### 2.1. NF-e (Nota Fiscal Eletrônica - Modelo 55)
* Comunicação via Web Services SEFAZ (HTTPS, TLS 1.2+, certificado digital ICP-Brasil A1).
* Tratamento estrito do schema XML (versão 4.00 com transição para 5.00).
* Eventos fiscais suportados:
  * Cancelamento (dentro do prazo legal do estado, tipicamente 24h).
  * Carta de Correção Eletrônica (CC-e) — proibida para dados cadastrais que alterem remetente/destinatário ou valores determinantes do imposto.
  * Inutilização de numeração.
  * Manifestação do Destinatário (Ciência da Operação, Confirmação, Desconhecimento, Operação não Realizada).
* Cálculo da Partilha de ICMS (Emenda Constitucional 87/2015 - Vendas para consumidor final não-contribuinte interestadual).

### 2.2. NFC-e (Nota Fiscal de Consumidor Eletrônica - Modelo 65)
* Substitui integralmente os antigos Emissores de Cupom Fiscal (ECF).
* Geração do QR-Code versão 2.0 (offline com contingência por contingência offline / EPEC).
* Impressão em bobina térmica (80mm ou 58mm) sem necessidade de equipamento homologado específico (apenas impressora não-fiscal comum).

---

## 3. Classificações Fiscais e Cadastros Base

Toda operação no sistema deve validar:
1. **CFOP (Código Fiscal de Operações e Prestações)**: Formato de 4 dígitos (1.xxx entrada estadual, 2.xxx interestadual, 3.xxx importação, 5.xxx saída estadual, 6.xxx saída interestadual, 7.xxx exportação).
2. **CST / CSOSN**:
   * Regime Normal (Lucro Presumido e Lucro Real): CST (Código de Situação Tributária) de 2 dígitos + origem (1 dígito).
   * Simples Nacional: CSOSN (Código de Situação da Operação no Simples Nacional) de 3 dígitos (101, 102, 201, 500, 900, etc.).
3. **NCM (Nomenclatura Comum do Mercosul)**: 8 dígitos numéricos obrigatórios em cada item de produto.
4. **CEST (Código Especificador da Substituição Tributária)**: 7 dígitos obrigatórios para produtos sujeitos a ICMS-ST.
5. **Tabela IBPT (Lei 12.741/2012)**: Cálculo automático do valor aproximado dos tributos federais, estaduais e municipais destacados no rodapé do documento.

---

## 4. Integração Financeira com Pagamentos Instantâneos (PIX BACEN)

* Conforme instruções normativas do BACEN e exigências das SEFAZ estaduais:
  * No pagamento via PIX, o campo `tPag` no XML da NF-e/NFC-e deve ser preenchido com `17` (PIX).
  * Deve ser informado se o QR Code foi dinâmico ou estático, CNPJ do intermediador e código de autorização da transação.

