/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.domain.models;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Factory for semi-monthly pay periods: 1–15 and 16–end-of-month.
 *
 * @author ACER
 */
public final class PayPeriodFactory {

    private PayPeriodFactory() {
    }

    public static PayPeriod firstHalf(YearMonth ym) {
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atDay(15);
        return new PayPeriod(start, end);
    }

    public static PayPeriod secondHalf(YearMonth ym) {
        LocalDate start = ym.atDay(16);
        LocalDate end = ym.atEndOfMonth();
        return new PayPeriod(start, end);
    }
}
