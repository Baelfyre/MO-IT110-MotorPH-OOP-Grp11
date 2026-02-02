/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.repository.csv;

/**
 * Centralized File Paths. Updated to include the Holiday Calendar.
 *
 * @author ACER
 */
public class DataPaths {

    // --- 1. CORE DATA ---
    public static final String EMPLOYEE_CSV = "./data/data_Employee.csv";
    public static final String LOGIN_CSV = "./data/data_Legacy_LogIn.csv";

    // --- 2. FOLDERS ---
    public static final String DTR_FOLDER = "./data/records_dtr/";
    public static final String PAYSLIP_FOLDER = "./data/records_payslips/";
    public static final String PAYROLL_FOLDER = "./data/records_payroll/";

    // --- 3. GOV TABLES ---
    public static final String GOV_SSS_CSV = "./data/gov_SSS_Table.csv";
    public static final String GOV_PHILHEALTH_CSV = "./data/gov_Philhealth_Table.csv";
    public static final String GOV_PAGIBIG_CSV = "./data/gov_Pagibig_Table.csv";
    public static final String GOV_TAX_CSV = "./data/gov_Tax_Table.csv";

    // --- 4. CALENDAR ---
    public static final String HOLIDAY_CSV = "./data/data_HolidayCalendar.csv";

    // --- 5. AUDIT LOGS ---
    public static final String LOG_PAYROLL = "./data/changeLogs_Payroll.csv";
    public static final String LOG_EMP_DATA = "./data/changeLogs_EmpDataChangeLogs.csv";
    public static final String LOG_DTR = "./data/changeLogs_DTR.csv";
    public static final String AUDIT_LOG_CSV = "./data/changeLogs_records.csv";
    public static final String SYSTEM_LOG_CSV = "./data/system_logs.csv";

    // --- 6. LEAVE ---
    public static final String LEAVE_CREDITS_CSV = "./data/data_LeaveCredits.csv";
    public static final String LEAVE_FOLDER = "./data/records_leave/";

    // Default Hased Password
    public static final String DEFAULT_PASSWORD = "Test1234";
}
