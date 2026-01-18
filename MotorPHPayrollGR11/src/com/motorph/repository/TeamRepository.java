/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.repository;

import com.motorph.domain.models.EmployeeProfile;
import java.util.List;

/**
 * Interface for retrieving team/subordinate structure.
 * @author ACER
 */
public interface TeamRepository {
    
    /**
     * Finds all employees who report to a specific supervisor.
     * @param supervisorName The name of the supervisor (e.g., "Lim, Antonio").
     * @return A list of EmployeeProfile objects belonging to that team.
     */
    List<EmployeeProfile> findSubordinates(String supervisorName);
}
