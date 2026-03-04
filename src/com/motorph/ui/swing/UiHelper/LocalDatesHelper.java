/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.ui.swing.UiHelper;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 *
 * @author ACER
* Date conversion helpers for Swing inputs.
 */
public final class LocalDatesHelper {

    private LocalDatesHelper() {
        // Annotation: Utility class.
    }

    // Annotation: Converts java.util.Date from Swing components into LocalDate.
    public static LocalDate toLocalDate(Date d) {
        if (d == null) return null;
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
