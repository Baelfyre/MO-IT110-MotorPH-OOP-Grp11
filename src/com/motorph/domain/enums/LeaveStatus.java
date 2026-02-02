/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.motorph.domain.enums;

/**
 *
 * @author OngoJ
 */
public enum LeaveStatus {
    PENDING,
    APPROVED,
    REJECTED;

    public static LeaveStatus fromCsv(String raw) {
        if (raw == null) {
            return PENDING;
        }
        String v = raw.trim();
        if (v.isEmpty()) {
            return PENDING;
        }
        try {
            return LeaveStatus.valueOf(v.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return PENDING;
        }
    }
}
