/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.domain.models;

import java.time.LocalDate;

/**
 * Represents a holiday record from the system calendar. Used to validate if a
 * specific date is a Regular or Special Non-working holiday.
 *
 * @author ACER
 */
public class Holiday {

    private LocalDate date;
    private String name;
    private String type; // "Regular Holiday" or "Special Non-working Holiday"

    public Holiday(LocalDate date, String name, String type) {
        this.date = date;
        this.name = name;
        this.type = type;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
