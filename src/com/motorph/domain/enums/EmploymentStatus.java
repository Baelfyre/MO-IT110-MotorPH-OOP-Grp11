/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.motorph.domain.enums;

/**
 *
 * @author ACER
 */
public enum EmploymentStatus {
    REGULAR("Regular"),
    PROBATIONARY("Probationary"),
    LEAVE("Leave"),
    REHIRE("Rehire"),
    RETIRED("Retired"),
    RESIGNED("Resigned"),
    TERMINATED("Terminated"),
    ARCHIVED("Archived");

    private final String label;

    EmploymentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
