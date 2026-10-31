# Visão de Produto Comercial 2026: Freedom ERP

---

## 1. Posicionamento Estratégico do Produto

O **Freedom ERP** modernizado é concebido para ser uma solução comercial **pronta para o mercado corporativo brasileiro**, direcionada a médias e pequenas empresas (PMEs), redes de varejo, distribuidores, atacados e indústrias leves que demandam:
* **Alta Confiabilidade e Performance Local**: Aplicação Desktop em Java 25 Swing com FlatLaf, imune à instabilidade de navegadores web para operações pesadas e frente de caixa (PDV).
* **Conformidade Tributária Rigorosa para 2026+**: Totalmente preparado para a transição da Reforma Tributária (Emenda Constitucional 132/2023), contemplando o cálculo simultâneo de CBS, IBS, Imposto Seletivo e os tributos legados (ICMS, ISS, PIS, COFINS).
* **Custos Operacionais Reduzidos**: Banco de dados open-source de padrão industrial (**PostgreSQL 16+**), eliminando licenças proprietárias caras de banco de dados e servidores de aplicação legados.
* **Experiência de Usuário Contemporânea**: Interface limpa, responsiva, em alta resolução (HiDPI 4K), com temas Claro/Escuro, navegação lateral rápida e dashboards operacionais.

---

## 2. Segmentos de Mercado Alvo

| Segmento | Necessidades Críticas Atendidas | Módulos Envolvidos |
| :--- | :--- | :--- |
| **Comércio Varejista & Franquias** | Emissão ultra-rápida de NFC-e, controle de caixa cego, integração com PIX e TEF moderno, controle de múltiplos estoques. | PDV, STD, GMS, FNC |
| **Atacado e Distribuidoras** | Faturamento em lote (NF-e 4.0/5.0), controle de lotes e validades, comissões em múltiplos níveis, tabelas de preços diferenciadas. | STD, GMS, FNC, LVF |
| **Indústrias Leves e Montadoras** | Estrutura de produtos multinível (BOM), ordens de produção (OP), empenho automático de insumos e custo padrão/real. | PCP, GMS, FNC, STD |
| **Prestadores de Serviços Técnicos** | Ordens de Serviço (OS), contratos recorrentes, faturamento de serviços e integração com NFS-e municipal. | ATD, FNC, STD, CRM |

---

## 3. Modelo de Implantação e Arquitetura de Distribuição

1. **Topologia Híbrida (On-Premise / Nuvem Privada)**:
   * **Banco de Dados Centralizado**: PostgreSQL 16+ rodando em servidor local do cliente ou em nuvem privada (AWS RDS, Google Cloud SQL, DigitalOcean, Hetzner ou servidor dedicado Linux).
   * **Estações de Trabalho (Clientes Ricos)**: Executáveis compilados nativamente via `jpackage` para Windows e Linux, sem necessidade de o cliente instalar manualmente o Java na máquina.
2. **Multi-Empresa e Multi-Filial**:
   * Suporte nativo a matriz e filiais compartilhando a mesma base de dados com isolamento transacional estrito por filial e consolidação contábil/financeira unificada na matriz.

---

## 4. Requisitos Não-Funcionais Globais

* **Tempo de Resposta**: Nenhuma tela de consulta comum pode demorar mais de 300ms para carregar dados paginados.
* **Disponibilidade do PDV**: O módulo de ponto de venda deve possuir modo de contingência offline para emissão de NFC-e mesmo na ausência temporária de internet.
* **Segurança e LGPD (Lei 13.709/2018)**:
  * Senhas criptografadas com algoritmo moderno `Argon2id` ou `BCrypt`.
  * Logs de auditoria imutáveis para qualquer alteração em cadastros de clientes, dados bancários e movimentações fiscais.
  * Mascaramento de dados sensíveis (CPF, RG, e-mails pessoais) em relatórios operacionais conforme o perfil de acesso do operador.

