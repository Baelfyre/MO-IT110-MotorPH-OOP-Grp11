/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.ui.swing.UiHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Small helper for building code-based Swing forms with GridBagLayout.
 *
 * @author ACER
 */
public final class SwingFormHelper {

    private SwingFormHelper() {
        // Annotation: Utility class.
    }

    public static JPanel createFormRoot(int pad) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new EmptyBorder(pad, pad, pad, pad));
        return p;
    }

    public static GridBagConstraints baseGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        return gbc;
    }

    public static void addLabel(JPanel p, GridBagConstraints base, int x, int y, String text) {
        GridBagConstraints c = (GridBagConstraints) base.clone();
        c.gridx = x;
        c.gridy = y;
        c.weightx = 0;
        p.add(new JLabel(text), c);
    }

    public static void addField(JPanel p, GridBagConstraints base, int x, int y, JComponent comp) {
        GridBagConstraints c = (GridBagConstraints) base.clone();
        c.gridx = x;
        c.gridy = y;
        c.weightx = 1;
        p.add(comp, c);
    }

    public static void addFieldSpan(JPanel p, GridBagConstraints base, int x, int y, int span, JComponent comp) {
        GridBagConstraints c = (GridBagConstraints) base.clone();
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = span;
        c.weightx = 1;
        p.add(comp, c);
    }

    public static JPanel wrapNorthCenterSouth(JComponent north, JComponent center, JComponent south) {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        if (north != null) {
            root.add(north, BorderLayout.NORTH);
        }
        if (center != null) {
            root.add(center, BorderLayout.CENTER);
        }
        if (south != null) {
            root.add(south, BorderLayout.SOUTH);
        }
        root.setBorder(new EmptyBorder(10, 10, 10, 10));
        return root;
    }
}
