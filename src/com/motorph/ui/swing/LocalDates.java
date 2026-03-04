package com.motorph.ui.swing;

import com.motorph.ui.swing.UiHelper.LocalDatesHelper;

import java.time.LocalDate;
import java.util.Date;

/**
 *
 * @author OngoJ.
 */
public final class LocalDates {

    private LocalDates() {
        // Annotation: Utility wrapper for LocalDatesHelper.
    }

    public static LocalDate toLocalDate(Date d) {
        return LocalDatesHelper.toLocalDate(d);
    }
}
