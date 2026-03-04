package com.motorph.ui.swing;

import com.motorph.ui.swing.UiHelper.SwingFormHelper;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author OngoJ.
 */
public final class SwingForm {

    private SwingForm() {
        // Annotation: Utility wrapper for SwingFormHelper.
    }

    public static JPanel createFormRoot(int pad) {
        return SwingFormHelper.createFormRoot(pad);
    }

    public static GridBagConstraints baseGbc() {
        return SwingFormHelper.baseGbc();
    }

    public static void addLabel(JPanel p, GridBagConstraints base, int x, int y, String text) {
        SwingFormHelper.addLabel(p, base, x, y, text);
    }

    public static void addField(JPanel p, GridBagConstraints base, int x, int y, JComponent comp) {
        SwingFormHelper.addField(p, base, x, y, comp);
    }

    public static void addFieldSpan(JPanel p, GridBagConstraints base, int x, int y, int span, JComponent comp) {
        SwingFormHelper.addFieldSpan(p, base, x, y, span, comp);
    }

    public static JPanel wrapNorthCenterSouth(JComponent north, JComponent center, JComponent south) {
        return SwingFormHelper.wrapNorthCenterSouth(north, center, south);
    }
}
