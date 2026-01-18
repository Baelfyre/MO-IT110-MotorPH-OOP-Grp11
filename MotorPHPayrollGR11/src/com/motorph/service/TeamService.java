/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.service;

import com.motorph.domain.models.EmployeeProfile;
import com.motorph.repository.TeamRepository;
import java.util.List;

/**
 * Service used by Managers to view their subordinates.
 * Bridges the UI and the TeamRepository.
 * @author ACER
 */
public class TeamService {
    
    private TeamRepository teamRepo;

    public TeamService(TeamRepository teamRepo) {
        this.teamRepo = teamRepo;
    }

    /**
     * Gets the list of employees assigned to a specific supervisor.
     * @param supervisorName The name of the logged-in manager (e.g., "Lim, Antonio").
     * @return List of employees.
     */
    public List<EmployeeProfile> getMyTeam(String supervisorName) {
        // You might add validation logic here if needed
        if (supervisorName == null || supervisorName.isEmpty()) {
            return List.of(); // Return empty list
        }
        return teamRepo.findSubordinates(supervisorName);
    }
}