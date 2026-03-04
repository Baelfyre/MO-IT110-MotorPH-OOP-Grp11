package com.motorph.ui.swing.UiHelper;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author ACER
 */
public final class UiThemeHelper {

    private static final Logger LOGGER = Logger.getLogger(UiThemeHelper.class.getName());

    private UiThemeHelper() {
        // Annotation: Utility class, no instances.
    }

    public static void useNimbus() {
        // Annotation: Apply Nimbus before any Swing UI is created.
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }

            // Annotation: Fallback to system LookAndFeel when Nimbus is not available.
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to set LookAndFeel.", ex);
        }
    }

    public static void refresh(Component root) {
        // Annotation: Update an already-built UI tree after changing LookAndFeel.
        if (root == null) return;
        SwingUtilities.updateComponentTreeUI(root);
        root.invalidate();
        root.validate();
        root.repaint();
    }
}
