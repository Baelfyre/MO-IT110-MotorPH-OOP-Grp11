/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.motorph.ops.time;

import com.motorph.domain.models.PayPeriod;
import com.motorph.domain.models.TimeEntry;

import java.util.List;

/**
 * Use-case boundary for time logging available to all roles.
 *
 * @author ACER
 */
public interface TimeOps {

    boolean clockIn(int empId);

    boolean clockOut(int empId);

    List<TimeEntry> viewMyTimeEntries(int empId);

    List<TimeEntry> viewMyTimeEntriesForPeriod(int empId, PayPeriod period);
}
