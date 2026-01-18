/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.service;

import com.motorph.domain.models.EmployeeProfile;
import com.motorph.domain.models.Payslip;
import com.motorph.repository.EmployeeRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service responsible for generating payroll summaries and reports. Aligned
 * with "MotorPH-Payroll Summary Report".
 *
 * @author ACER
 */
public class ReportGenerationService {

    private EmployeeRepository employeeRepo;

    public ReportGenerationService(EmployeeRepository employeeRepo) {
        this.employeeRepo = employeeRepo;
    }

    /**
     * Generates a detailed console report matching the PDF columns. In the
     * future, this can be converted to export to PDF/CSV.
     *
     * @param payslips The list of payslips for the period.
     */
    public void generateSummaryReport(List<Payslip> payslips) {
        System.out.println("======================================================================================================");
        System.out.println("                                MONTHLY PAYROLL SUMMARY REPORT                                        ");
        System.out.println("======================================================================================================");
        System.out.printf("%-10s %-20s %-15s %-15s %-12s %-10s %-10s %-10s %-10s %-12s\n",
                "Emp No", "Name", "Position", "Dept", "Gross", "SSS", "PhilH", "PagIBIG", "Tax", "Net Pay");
        System.out.println("------------------------------------------------------------------------------------------------------");

        double totalGross = 0;
        double totalNet = 0;

        for (Payslip p : payslips) {
            EmployeeProfile emp = employeeRepo.findByEmployeeNumber(p.getEmployeeId());

            String name = (emp != null) ? emp.getLastName() + ", " + emp.getFirstName() : "Unknown";
            // Assuming Position/Dept are stored in EmployeeProfile or derived
            String position = "N/A";
            String dept = "N/A";

            // Note: If you add Position/Department to EmployeeProfile later, access them here:
            // position = emp.getPosition();
            // dept = emp.getDepartment();
            System.out.printf("%-10d %-20.20s %-15.15s %-15.15s %-12.2f %-10.2f %-10.2f %-10.2f %-10.2f %-12.2f\n",
                    p.getEmployeeId(),
                    name,
                    position,
                    dept,
                    p.getGrossIncome(),
                    p.getSss(),
                    p.getPhilHealth(),
                    p.getPagIbig(),
                    p.getWithholdingTax(),
                    p.getNetPay()
            );

            totalGross += p.getGrossIncome();
            totalNet += p.getNetPay();
        }

        System.out.println("------------------------------------------------------------------------------------------------------");
        System.out.printf("%-60s %-12.2f %-40s %-12.2f\n", "TOTALS:", totalGross, "", totalNet);
        System.out.println("======================================================================================================");
    }

    /**
     * Aggregates totals by Department (as mentioned in Architecture).
     *
     * @param payslips List of payslips
     * @return A Map where Key = Department Name, Value = Total Net Pay
     */
    public Map<String, Double> getDepartmentTotals(List<Payslip> payslips) {
        // This requires EmployeeProfile to have a getDepartment() method.
        // Since we are using basic CSV, this logic groups by "Unknown" if field is missing.
        return payslips.stream().collect(Collectors.groupingBy(p -> {
            EmployeeProfile emp = employeeRepo.findByEmployeeNumber(p.getEmployeeId());
            return (emp != null && emp.getImmediateSupervisor() != null) ? "General" : "Unknown"; // Placeholder logic
        }, Collectors.summingDouble(Payslip::getNetPay)));
    }
}
