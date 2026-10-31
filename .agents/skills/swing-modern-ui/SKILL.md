---
name: swing-modern-ui
description: >-
  Padrões de design system e componentes modernos em Java Swing com FlatLaf.
  Instruções para MigLayout, ModernSidebar retrátil, ModernDataTable (paginada com filtro global),
  ModernCard (KPIs), temas Light/Dark e renderização vetorial SVG sem travamento de EDT.
---

# Design System Java Swing Moderno (FlatLaf + MigLayout)

Este skill define os componentes visuais, padrões de usabilidade e arquitetura de interface gráfica desktop para a modernização do Freedom ERP.

## 1. Princípios da UI Moderna em Swing

1. **HiDPI e 4K Nativos**:
   * O **FlatLaf** recalcula automaticamente fontes, bordas e ícones conforme o fator de escala do Windows e Linux (100%, 125%, 150%, 200%).
   * Substituição de todos os arquivos `.gif` e `.png` rasterizados por ícones vetoriais SVG via `FlatSVGIcon`.
2. **Separação Rígida entre Tela e Negócio**:
   * A tela Swing nunca instancia `PreparedStatement`, `Connection` ou executa queries SQL diretamente.
   * A tela se comunica exclusivamente com classes `@Service` através de DTOs e Records.
3. **Nunca Travar a EDT (Event Dispatch Thread)**:
   * Qualquer chamada a banco de dados ou API externa é despachada em uma **Virtual Thread** (`Thread.ofVirtual().start(...)`).
   * A atualização dos componentes visuais retorna à EDT via `SwingUtilities.invokeLater(...)`.

---

## 2. Catálogo de Componentes Personalizados

### 2.1. `ModernSidebar`
* Menu lateral esquerdo expansível e recolhível (toggle via botão ou atalho `Ctrl + B`).
* Campo de busca rápida no topo com atalho `Ctrl + K` para filtragem instantânea de telas e relatórios.
* Seções colapsáveis com cabeçalhos elegantes (Vendas, Financeiro, Estoque, PCP, Fiscal).
* Rodapé com foto/avatar do operador, nome do usuário, filial ativa e chaveador de tema Light/Dark.

### 2.2. `ModernDataTable<T>`
* Tabela construída sobre `JTable` com `AbstractTableModel` fortemente tipado:
  * Linhas com altura confortável (padding de 32px por linha) e efeito hover.
  * Linhas zebradas alternadas suaves.
  * Barra de ferramentas superior com:
    * Campo de busca textual global instantâneo (`RowFilter`).
    * Seletor de colunas visíveis.
    * Botões de ação rápida: Adicionar, Editar, Excluir, Exportar CSV e Excel.
  * Rodapé com paginação sob demanda (Primeira, Anterior, Número de Páginas, Próxima, Última) e contador de registros totais.
  * Renderizadores de células personalizados:
    * Badges coloridos com cantos arredondados para status (ex: Verde para *Quitado*, Vermelho para *Vencido*, Amarelo para *Pendente*).
    * Alinhamento numérico à direita com máscara monetária formatada (`R$ #,##0.00`).

### 2.3. `ModernCard` (KPI & Métricas)
* Painéis estilizados para dashboards executivos e telas de abertura:
  * Bordas com cantos arredondados (Arc de 12px) e sombra sutil.
  * Título da métrica (fonte 12px, cor secundária).
  * Valor em destaque (fonte 22px Bold, cor primária).
  * Ícone temático no canto superior direito.
  * Indicador de tendência (ex: `▲ +12,4% vs mês anterior` em verde ou `▼ -3,1%` em vermelho).

---

## 3. Substituição de Null Layout por MigLayout

O código legado utiliza posicionamento manual absoluto:
```java
// LEGADO: Quebra em resoluções diferentes e telas modernas
adicCampo(txtNome, 10, 40, 250, 20, "Nome", "Nome do Cliente", ...);
```

No novo padrão, utilizamos **MigLayout**:
```java
// MODERNO: Fluido, responsivo e adaptável
panel.setLayout(new MigLayout("wrap 4, fillx, insets 16", "[grow 10][grow 40][grow 25][grow 25]"));
panel.add(new JLabel("Código"), "cell 0 0");
panel.add(txtCodigo, "cell 0 1, growx");
panel.add(new JLabel("Nome / Razão Social"), "cell 1 0");
panel.add(txtNome, "cell 1 1, growx");
```

