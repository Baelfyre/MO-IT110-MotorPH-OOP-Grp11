/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.domain.models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 *
 * @author OngoJ.
 */
public abstract class Employee {

    private int employeeNumber;
    private String lastName;
    private String firstName;

    private EmployeeDetails employeeDetails;
    private Position position;
    private Compensation compensation;

    protected Employee(int employeeNumber, String lastName, String firstName) {
        setEmployeeNumber(employeeNumber);
        setLastName(lastName);
        setFirstName(firstName);
        this.employeeDetails = new EmployeeDetails();
        this.position = new Position();
        this.compensation = new Compensation();
    }

    public abstract double calculateLeaveCredits();

    // Annotation: Legacy compatibility getter used by repositories and Ops.
    public int getId() {
        return employeeNumber;
    }

    // Annotation: Legacy compatibility setter used by repositories and Ops.
    public void setId(int id) {
        setEmployeeNumber(id);
    }

    public int getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(int employeeNumber) {
        if (employeeNumber <= 0) {
            throw new IllegalArgumentException("Employee number must be > 0.");
        }
        this.employeeNumber = employeeNumber;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = safe(lastName);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = safe(firstName);
    }

    public EmployeeDetails getEmployeeDetails() {
        return employeeDetails;
    }

    public void setEmployeeDetails(EmployeeDetails employeeDetails) {
        this.employeeDetails = (employeeDetails == null) ? new EmployeeDetails() : employeeDetails;
    }

    // Annotation: Returns position title for UI, Ops, and CSV.
    public String getPosition() {
        return (position == null) ? "" : safe(position.getJobTitle());
    }

    // Annotation: Accessor for the Position value object.
    public Position getPositionObj() {
        return position;
    }

    // Annotation: Legacy setter that accepts job title from CSV/UI.
    public void setPosition(String jobTitle) {
        if (this.position == null) {
            this.position = new Position();
        }
        this.position.setJobTitle(jobTitle);
    }

    public void setPosition(Position position) {
        this.position = (position == null) ? new Position() : position;
    }

    public Compensation getCompensation() {
        return compensation;
    }

    public void setCompensation(Compensation compensation) {
        this.compensation = (compensation == null) ? new Compensation() : compensation;
    }

    // -----------------------------
    // Legacy compatibility accessors
    // -----------------------------

    // Annotation: Status stored in Position.employmentStatus.
    public String getStatus() {
        return (position == null) ? "" : safe(position.getEmploymentStatus());
    }

    // Annotation: Status stored in Position.employmentStatus.
    public void setStatus(String status) {
        if (this.position == null) {
            this.position = new Position();
        }
        this.position.setEmploymentStatus(status);
    }

    // Annotation: Immediate supervisor stored in Position.immediateSupervisor.
    public String getImmediateSupervisor() {
        return (position == null) ? "" : safe(position.getImmediateSupervisor());
    }

    // Annotation: Immediate supervisor stored in Position.immediateSupervisor.
    public void setImmediateSupervisor(String supervisor) {
        if (this.position == null) {
            this.position = new Position();
        }
        this.position.setImmediateSupervisor(supervisor);
    }

    // Annotation: Delegates to EmployeeDetails.
    public LocalDate getBirthday() {
        return (employeeDetails == null) ? null : employeeDetails.getBirthday();
    }

    // Annotation: Delegates to EmployeeDetails.
    public void setBirthday(LocalDate birthday) {
        if (this.employeeDetails == null) {
            this.employeeDetails = new EmployeeDetails();
        }
        this.employeeDetails.setBirthday(birthday);
    }

    public String getAddress() {
        return (employeeDetails == null) ? "" : safe(employeeDetails.getAddress());
    }

    public void setAddress(String address) {
        if (this.employeeDetails == null) {
            this.employeeDetails = new EmployeeDetails();
        }
        this.employeeDetails.setAddress(address);
    }

    public String getPhoneNumber() {
        return (employeeDetails == null) ? "" : safe(employeeDetails.getPhoneNumber());
    }

    public void setPhoneNumber(String phoneNumber) {
        if (this.employeeDetails == null) {
            this.employeeDetails = new EmployeeDetails();
        }
        this.employeeDetails.setPhoneNumber(phoneNumber);
    }

    public String getSssNumber() {
        return (employeeDetails == null) ? "" : safe(employeeDetails.getSssNumber());
    }

    public void setSssNumber(String sssNumber) {
        if (this.employeeDetails == null) {
            this.employeeDetails = new EmployeeDetails();
        }
        this.employeeDetails.setSssNumber(sssNumber);
    }

    public String getPhilHealthNumber() {
        return (employeeDetails == null) ? "" : safe(employeeDetails.getPhilHealthNumber());
    }

    public void setPhilHealthNumber(String philHealthNumber) {
        if (this.employeeDetails == null) {
            this.employeeDetails = new EmployeeDetails();
        }
        this.employeeDetails.setPhilHealthNumber(philHealthNumber);
    }

    public String getTinNumber() {
        return (employeeDetails == null) ? "" : safe(employeeDetails.getTinNumber());
    }

    public void setTinNumber(String tinNumber) {
        if (this.employeeDetails == null) {
            this.employeeDetails = new EmployeeDetails();
        }
        this.employeeDetails.setTinNumber(tinNumber);
    }

    public String getPagIbigNumber() {
        return (employeeDetails == null) ? "" : safe(employeeDetails.getPagIbigNumber());
    }

    public void setPagIbigNumber(String pagIbigNumber) {
        if (this.employeeDetails == null) {
            this.employeeDetails = new EmployeeDetails();
        }
        this.employeeDetails.setPagIbigNumber(pagIbigNumber);
    }


    public String getEmail() {
        return (employeeDetails == null) ? "" : safe(employeeDetails.getEmail());
    }

    public void setEmail(String email) {
        if (this.employeeDetails == null) {
            this.employeeDetails = new EmployeeDetails();
        }
        this.employeeDetails.setEmail(email);
    }

    // Annotation: Compensation wrappers using legacy field names.
    public double getBasicSalary() {
        return (compensation == null) ? 0.0 : compensation.getBasicSalary();
    }

    public void setBasicSalary(double basicSalary) {
        if (this.compensation == null) {
            this.compensation = new Compensation();
        }
        this.compensation.setBasicSalary(basicSalary);
    }

    public double getRiceAllowance() {
        return (compensation == null) ? 0.0 : compensation.getRiceSubsidy();
    }

    public void setRiceAllowance(double riceAllowance) {
        if (this.compensation == null) {
            this.compensation = new Compensation();
        }
        this.compensation.setRiceSubsidy(riceAllowance);
    }

    public double getPhoneAllowance() {
        return (compensation == null) ? 0.0 : compensation.getPhoneAllowance();
    }

    public void setPhoneAllowance(double phoneAllowance) {
        if (this.compensation == null) {
            this.compensation = new Compensation();
        }
        this.compensation.setPhoneAllowance(phoneAllowance);
    }

    public double getClothingAllowance() {
        return (compensation == null) ? 0.0 : compensation.getClothingAllowance();
    }

    public void setClothingAllowance(double clothingAllowance) {
        if (this.compensation == null) {
            this.compensation = new Compensation();
        }
        this.compensation.setClothingAllowance(clothingAllowance);
    }

    public double getGrossSemiMonthlyRate() {
        return (compensation == null) ? 0.0 : compensation.getGrossSemiMonthlyRate();
    }

    public void setGrossSemiMonthlyRate(double grossSemiMonthlyRate) {
        if (this.compensation == null) {
            this.compensation = new Compensation();
        }
        this.compensation.setGrossSemiMonthlyRate(grossSemiMonthlyRate);
    }

    public double getHourlyRate() {
        return (compensation == null) ? 0.0 : compensation.getHourlyRate();
    }

    public void setHourlyRate(double hourlyRate) {
        if (this.compensation == null) {
            this.compensation = new Compensation();
        }
        this.compensation.setHourlyRate(hourlyRate);
    }

    // Annotation: Serializes the employee in the same column order as data_Employee.csv.
    public String toCsvRow() {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("M/d/yyyy", Locale.US);
        String bday = (getBirthday() == null) ? "" : getBirthday().format(df);

        return String.join(",",
                String.valueOf(getEmployeeNumber()),
                escape(getLastName()),
                escape(getFirstName()),
                escape(bday),
                escape(getAddress()),
                escape(getPhoneNumber()),
                escape(getSssNumber()),
                escape(getPhilHealthNumber()),
                escape(getTinNumber()),
                escape(getPagIbigNumber()),
                escape(getStatus()),
                escape(getPosition()),
                escape(getImmediateSupervisor()),
                fmt(getBasicSalary()),
                fmt(getRiceAllowance()),
                fmt(getPhoneAllowance()),
                fmt(getClothingAllowance()),
                fmt(getGrossSemiMonthlyRate()),
                fmt(getHourlyRate()),
                escape(getEmail())
        );
    }

    private String escape(String v) {
        if (v == null) {
            return "";
        }
        String s = v.replace("\"", "\"\"");
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s + "\"";
        }
        return s;
    }

    private String fmt(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) {
            return "0";
        }
        long rounded = Math.round(v);
        if (Math.abs(v - rounded) < 0.0000001) {
            return Long.toString(rounded);
        }
        return String.format(Locale.US, "%.2f", v);
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
