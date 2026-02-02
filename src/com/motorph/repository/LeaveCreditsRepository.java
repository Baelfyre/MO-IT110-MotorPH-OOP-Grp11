/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.motorph.repository;

import com.motorph.domain.models.LeaveCredits;
import java.util.List;

/**
 * Contract for leave credits storage. Credits are expressed in hours.
 *
 * @author ACER
 */
public interface LeaveCreditsRepository {

    List<LeaveCredits> findAll();

    LeaveCredits findByEmpId(int empId);

    boolean updateLeaveTaken(int empId, double leaveTakenHours);
}
