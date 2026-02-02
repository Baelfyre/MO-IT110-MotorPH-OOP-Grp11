/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.service;

import com.motorph.domain.models.PayPeriod;
import com.motorph.repository.LeaveCreditsRepository;
import java.time.LocalDate;

/**
 * Combines stored leave credits with computed leave usage to determine
 * balances.
 *
 * @author ACER
 */
public class LeaveCreditsService {

    private final LeaveCreditsRepository creditsRepo;
    private final LeaveService leaveService;

    public LeaveCreditsService(LeaveCreditsRepository creditsRepo, LeaveService leaveService) {
        this.creditsRepo = creditsRepo;
        this.leaveService = leaveService;
    }

    public double getLeaveUsedThisPeriod(int empId, PayPeriod period) {
        return leaveService.getLeaveHoursUsed(empId, period);
    }

    public double getLeaveTakenYearToDate(int empId, PayPeriod period) {
        LocalDate yearStart = LocalDate.of(period.getEndDate().getYear(), 1, 1);
        PayPeriod ytd = new PayPeriod(yearStart, period.getEndDate());
        return leaveService.getLeaveHoursUsed(empId, ytd);
    }

    public double getRemainingCreditsYearToDate(int empId, PayPeriod period) {
        var credits = creditsRepo.findByEmpId(empId);
        if (credits == null) {
            return 0.0;
        }

        double takenYtd = getLeaveTakenYearToDate(empId, period);
        return Math.max(0.0, credits.getLeaveCreditsHours() - takenYtd);
    }

    public boolean syncLeaveTakenYearToDate(int empId, PayPeriod period) {
        double takenYtd = getLeaveTakenYearToDate(empId, period);
        return creditsRepo.updateLeaveTaken(empId, takenYtd);
    }
}
