package org.freedom.ui.app.view.stock;

import org.freedom.core.service.stock.EstoqueRepository;
import org.freedom.core.service.stock.EstoqueService;
import org.freedom.ui.components.table.ModernDataTable;
import org.freedom.ui.components.table.ModernTableModel;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

/**
 * Painel completo de Gestão de Estoque e Almoxarifado:
 * - Visão geral de saldos por produto
 * - Custo Médio Ponderado e Valor Total em Estoque
 * - Botão de Entrada de Mercadorias
 */
public class EstoqueManagementPanel extends JPanel {

    private final EstoqueService service;
    private final ModernDataTable<EstoqueRepository.SaldoVisualizacaoDto> dataTable;
    private final ModernTableModel<EstoqueRepository.SaldoVisualizacaoDto> tableModel;
    private final JFrame parentFrame;

    public EstoqueManagementPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.service = new EstoqueService();

        setLayout(new BorderLayout(12, 12));

        // 1. Barra de Ações
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JButton btnEntrada = new JButton("📥 Entrada de Mercadoria");
        JButton btnAtualizar = new JButton("🔄 Recarregar");

        actionPanel.add(btnEntrada);
        actionPanel.add(btnAtualizar);
        add(actionPanel, BorderLayout.NORTH);

        // 2. Tabela Moderna Fortemente Tipada
        tableModel = new ModernTableModel<>(
            List.of("ID", "Produto / Mercadoria", "UN", "Saldo em Estoque", "Custo Médio (R$)", "Valor Total em Estoque (R$)"),
            List.of(
                s -> s.produtoId() != null ? s.produtoId().toString() : "",
                EstoqueRepository.SaldoVisualizacaoDto::descricaoProduto,
                EstoqueRepository.SaldoVisualizacaoDto::unidadeMedida,
                s -> s.quantidadeAtual() != null ? s.quantidadeAtual().toString() : "0.0000",
                s -> s.custoMedioPonderado() != null ? "R$ " + s.custoMedioPonderado() : "R$ 0,00",
                s -> s.valorTotalEstoque() != null ? "R$ " + s.valorTotalEstoque() : "R$ 0,00"
            )
        );

        dataTable = new ModernDataTable<>(tableModel);
        add(dataTable, BorderLayout.CENTER);

        // Eventos
        btnEntrada.addActionListener(e -> {
            EntradaEstoqueDialog dialog = new EntradaEstoqueDialog(parentFrame, service, this::recarregarSaldos);
            dialog.setVisible(true);
        });

        btnAtualizar.addActionListener(e -> recarregarSaldos());

        recarregarSaldos();
    }

    public void recarregarSaldos() {
        service.listarSaldosAsync()
            .thenAccept(saldos -> SwingUtilities.invokeLater(() -> dataTable.setData(saldos)));
    }
}

