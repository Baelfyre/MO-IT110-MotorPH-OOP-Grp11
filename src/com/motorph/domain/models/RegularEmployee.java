/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.domain.models;

/**
 *
 * @author OngoJ.
 */
public class RegularEmployee extends Employee {

    public RegularEmployee(int employeeNumber, String lastName, String firstName) {
        super(employeeNumber, lastName, firstName);
    }

    @Override
    public double calculateLeaveCredits() {
        // Annotation: Regular accrual value is configurable by business rules.
        return 1.0;
    }
}
