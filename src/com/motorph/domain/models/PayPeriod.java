/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.domain.models;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Immutable pay period value object. Scheme: semi-monthly (1–15,
 * 16–end-of-month).
 *
 * @author ACER
 */
public class PayPeriod {

    private static final DateTimeFormatter KEY_FMT = DateTimeFormatter.ofPattern("yyMMdd");

    private final LocalDate startDate;
    private final LocalDate endDate;

    public PayPeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("PayPeriod dates cannot be null.");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("PayPeriod endDate cannot be before startDate.");
        }
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public boolean includes(LocalDate date) {
        if (date == null) {
            return false;
        }
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Key used for filenames: yyMMdd-yyMMdd (example: 240601-240630)
     */
    public String toKey() {
        return startDate.format(KEY_FMT) + "-" + endDate.format(KEY_FMT);
    }

    /**
     * Semi-monthly factory for any date: 1–15, 16–end-of-month.
     */
    public static PayPeriod fromDateSemiMonthly(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("date cannot be null.");
        }

        int day = date.getDayOfMonth();
        YearMonth ym = YearMonth.of(date.getYear(), date.getMonth());

        LocalDate start = (day <= 15)
                ? LocalDate.of(date.getYear(), date.getMonth(), 1)
                : LocalDate.of(date.getYear(), date.getMonth(), 16);

        LocalDate end = (day <= 15)
                ? LocalDate.of(date.getYear(), date.getMonth(), 15)
                : LocalDate.of(date.getYear(), date.getMonth(), ym.lengthOfMonth());

        return new PayPeriod(start, end);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PayPeriod)) {
            return false;
        }
        PayPeriod other = (PayPeriod) o;
        return Objects.equals(startDate, other.startDate)
                && Objects.equals(endDate, other.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startDate, endDate);
    }

    @Override
    public String toString() {
        return "PayPeriod{" + startDate + " to " + endDate + "}";
    }
}
