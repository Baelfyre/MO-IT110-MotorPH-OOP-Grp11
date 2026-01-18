/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.repository;

import com.motorph.domain.models.Holiday;
import java.time.LocalDate;

/**
 * Interface defining operations for accessing Holiday data.
 * @author ACER
 */
public interface HolidayRepository {
    
    /**
     * Finds a holiday by its date.
     * @param date The date to check.
     * @return The Holiday object if found, otherwise null.
     */
    Holiday findByDate(LocalDate date);
}
