package org.freedom.ui.components.theme;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Component;

/**
 * Gerenciador Central de Temas e Design System Moderno Swing via FlatLaf.
 * Oferece suporte nativo a monitores HiDPI/4K e chaveamento dinâmico Light/Dark.
 */
public final class ThemeManager {

    private static final Logger log = LoggerFactory.getLogger(ThemeManager.class);
    private static boolean isDarkMode = false;

    private ThemeManager() {
        // Utilitário estático
    }

    public static void setup() {
        // Habilita renderização de cantos arredondados e fontes HiDPI nativas
        System.setProperty("flatlaf.uiScale.enabled", "true");
        System.setProperty("flatlaf.animation", "true");

        applyTheme(false);
    }

    public static void toggleTheme(Component rootComponent) {
        applyTheme(!isDarkMode);
        if (rootComponent != null) {
            SwingUtilities.updateComponentTreeUI(rootComponent);
        }
    }

    public static void applyTheme(boolean dark) {
        try {
            if (dark) {
                FlatDarkLaf.setup();
                isDarkMode = true;
                log.info("Tema escuro (FlatDarkLaf) ativado com sucesso.");
            } else {
                FlatLightLaf.setup();
                isDarkMode = false;
                log.info("Tema claro (FlatLightLaf) ativado com sucesso.");
            }

            // Customizações padrão para ERP corporativo
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("Table.rowHeight", 32);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("ScrollBar.showButtons", false);
            UIManager.put("ScrollBar.width", 10);

        } catch (Exception e) {
            log.error("Falha ao configurar tema FlatLaf: {}", e.getMessage(), e);
        }
    }

    public static boolean isDarkMode() {
        return isDarkMode;
    }
}

