package com.motorph.ui.swing.UiHelper;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import com.formdev.flatlaf.FlatIntelliJLaf;
import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Utility class for applying and refreshing Swing UI themes.
 *
 * @author ACER
 */
public final class UiThemeHelper {

    private static final Logger LOGGER = Logger.getLogger(UiThemeHelper.class.getName());

    private UiThemeHelper() {
        // Annotation: Utility class, no instances.
    }

    public static void useFlatLaf() {
        // Annotation: Apply FlatLaf before any Swing UI is created.
        try {
            UIManager.setLookAndFeel(new FlatIntelliJLaf());

            // Annotation: Optional UI tuning for a cleaner modern look.
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 12);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("ProgressBar.arc", 10);
            UIManager.put("ScrollBar.width", 10);

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to set FlatLaf.", ex);
        }
    }

    public static void refresh(Component root) {
        // Annotation: Update an already-built UI tree after changing LookAndFeel.
        if (root == null) {
            return;
        }

        SwingUtilities.updateComponentTreeUI(root);
        root.invalidate();
        root.validate();
        root.repaint();
    }
}
