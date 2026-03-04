/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.domain.models;

/**
 *
 * @author OngoJ.
 */
public class Compensation {

    private double basicSalary;
    private double hourlyRate;
    private double riceSubsidy;
    private double phoneAllowance;
    private double clothingAllowance;
    private double grossSemiMonthlyRate;

    public Compensation() {
    }

    public double getBasicSalary() {
        return basicSalary;
    }

    public void setBasicSalary(double basicSalary) {
        requireNonNegative(basicSalary, "basicSalary");
        this.basicSalary = basicSalary;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(double hourlyRate) {
        // Annotation: Allow 0.0 for seed/testing rows.
        requireNonNegative(hourlyRate, "hourlyRate");
        this.hourlyRate = hourlyRate;
    }

    public double getRiceSubsidy() {
        return riceSubsidy;
    }

    public void setRiceSubsidy(double riceSubsidy) {
        requireNonNegative(riceSubsidy, "riceSubsidy");
        this.riceSubsidy = riceSubsidy;
    }

    public double getPhoneAllowance() {
        return phoneAllowance;
    }

    public void setPhoneAllowance(double phoneAllowance) {
        requireNonNegative(phoneAllowance, "phoneAllowance");
        this.phoneAllowance = phoneAllowance;
    }

    public double getClothingAllowance() {
        return clothingAllowance;
    }

    public void setClothingAllowance(double clothingAllowance) {
        requireNonNegative(clothingAllowance, "clothingAllowance");
        this.clothingAllowance = clothingAllowance;
    }

    public double getGrossSemiMonthlyRate() {
        return grossSemiMonthlyRate;
    }

    public void setGrossSemiMonthlyRate(double grossSemiMonthlyRate) {
        // Annotation: Allow 0.0 for seed/testing rows.
        requireNonNegative(grossSemiMonthlyRate, "grossSemiMonthlyRate");
        this.grossSemiMonthlyRate = grossSemiMonthlyRate;
    }

    private void requireNonNegative(double v, String field) {
        if (v < 0.0) {
            throw new IllegalArgumentException(field + " must be >= 0.0");
        }
    }

    private void requirePositive(double v, String field) {
        if (v <= 0.0) {
            throw new IllegalArgumentException(field + " must be > 0.0");
        }
    }
}
