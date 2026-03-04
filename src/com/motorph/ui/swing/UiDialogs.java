package com.motorph.ui.swing;

import com.motorph.ui.swing.UiHelper.UiDialogsHelper;

/**
 *
 * @author OngoJ.
 */
public final class UiDialogs {

    private UiDialogs() {
        // Annotation: Utility wrapper for UiDialogsHelper.
    }

    public static void info(java.awt.Component parent, String message) {
        UiDialogsHelper.info(parent, message);
    }

    public static void warn(java.awt.Component parent, String message) {
        UiDialogsHelper.warn(parent, message);
    }

    public static void error(java.awt.Component parent, String message) {
        UiDialogsHelper.error(parent, message);
    }

    public static boolean confirm(java.awt.Component parent, String message) {
        return UiDialogsHelper.confirm(parent, message);
    }
}
