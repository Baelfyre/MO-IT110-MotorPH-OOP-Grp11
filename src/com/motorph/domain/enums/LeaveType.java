/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.motorph.domain.enums;
import java.util.Locale;

/**
 *
 * @author JC
 */
public enum LeaveType {
    VACATION("Vacation Leave"),
    SICK("Sick Leave"),
    EMERGENCY("Emergency Leave"),
    UNPAID("Unpaid Leave");

    private final String label;

    LeaveType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static LeaveType fromCsv(String raw) {
        if (raw == null || raw.isBlank()) {
            return VACATION;
        }
        String value = raw.trim().toUpperCase(Locale.US);
        for (LeaveType type : values()) {
            if (type.name().equals(value)) {
                return type;
            }
        }
        return VACATION;
    }
}
