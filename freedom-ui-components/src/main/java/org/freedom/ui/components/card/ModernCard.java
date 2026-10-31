package org.freedom.ui.components.card;

import com.formdev.flatlaf.ui.FlatBorder;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

/**
 * Componente de Card Moderno para Dashboards Executivos e KPIs corporativos.
 */
public class ModernCard extends JPanel {

    private final JLabel lblTitle;
    private final JLabel lblValue;
    private final JLabel lblVariation;

    public ModernCard(String title, String initialValue, String variation, boolean isPositiveTrend) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createCompoundBorder(
            new FlatBorder(),
            BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));

        lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTitle.setForeground(new Color(110, 115, 125));

        lblValue = new JLabel(initialValue);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 22));

        lblVariation = new JLabel(variation);
        lblVariation.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblVariation.setForeground(isPositiveTrend ? new Color(34, 139, 34) : new Color(220, 20, 60));

        JPanel contentPanel = new JPanel(new GridLayout(3, 1, 4, 4));
        contentPanel.setOpaque(false);
        contentPanel.add(lblTitle);
        contentPanel.add(lblValue);
        contentPanel.add(lblVariation);

        add(contentPanel, BorderLayout.CENTER);
    }

    public void updateValue(String newValue, String newVariation, boolean isPositiveTrend) {
        lblValue.setText(newValue);
        lblVariation.setText(newVariation);
        lblVariation.setForeground(isPositiveTrend ? new Color(34, 139, 34) : new Color(220, 20, 60));
        repaint();
    }
}

