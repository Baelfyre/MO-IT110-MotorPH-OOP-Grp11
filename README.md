# MO-IT110-MotorPH-OOP-Grp11

MotorPH Payroll and Inventory System (OOP) for **MO-IT110**.  
Built in Java using layered architecture (Domain, Repository, Service, UI) with CSV-backed persistence and role-based screens.

---

## Members
- Juan Carlos Manalastas
- James Lynelle Ongo
- Jason Bryan Tan
- Sean Gil Quiben

---

## Project Architecture (Aligned with MotorPH BRS)

This repository follows a layered, object-oriented architecture aligned with the MotorPH **Business Requirement Specification (BRS)** and Information Management principles. The design prioritizes:

- **Regulatory Compliance**: payroll deductions and statutory contributions are computed using government reference tables (SSS Circular 2024-006-derived table, PhilHealth, Pag-IBIG, and withholding tax tables).
- **Data Security (RBAC)**: access to features and sensitive payroll data is restricted by user roles (**Employee, HR, IT, Manager, Payroll**) through centralized access control.
- **Reporting**: payroll outputs support generation of management reports such as the **Monthly Payroll Summary Report** (e.g., totals per department for SSS and tax).

All ticketing modules have been removed to keep the scope focused on payroll compliance, role-based access, and reporting.

---

## Package Overview

### 1) `com.motorph.domain.enum`
_Defining constants to ensure data integrity._

- **`Role`**: Defines system roles (**Employee, HR, IT, Manager, Payroll**) to enforce role-based access across dashboards and services.
- **`ApprovalStatus`**: Standardizes workflow states (**Pending, Approved, Rejected**) used for timecard approval and validation.

---

### 2) `com.motorph.domain.models`
_Rich domain models designed to support payroll computation, compliance, and reporting._

- **`EmployeeProfile`**: Master employee record. Stores personal details, salary base, and **department** (used for Summary Report grouping).
- **`UserAccount`**: Links to `EmployeeProfile`. Stores credentials and assigned `Role` to support RBAC security (including lockout status if implemented).
- **`PayPeriod`**: Represents payroll cut-off windows (start/end dates) used to group time entries, timecards, and payslips consistently.
- **`Timecard`**: **Aggregate root.** Contains a list of `TimeEntry` records for a pay period/week and is the unit submitted for manager approval.
- **`TimeEntry`**: One day log (Time In/Time Out). Includes checks for **workday vs weekend** and supports holiday tagging.
- **`Holiday`** *(New)*: Stores holiday dates and type (Regular/Special) to automate holiday tagging and pay eligibility rules (including double-pay logic if required).
- **`Payslip`** *(Refactored Composite)*: Represents payroll results for an employee and pay period. Structured to match the payslip layout using:
  - **`Earnings`** (Rate, Days Worked, Overtime)
  - **`Benefits`** (Rice, Clothing, Phone)
  - **`Deductions`** (SSS, PhilHealth, **Pag-IBIG**, Withholding Tax)
- **`AuditLogEntry`**: Records security-relevant actions and key system events (e.g., failed logins, payroll runs/overrides, approvals) for traceability.
- **`DtrChangeLogEntry`**: Captures edits made to DTR/time records (what changed, who changed it, and when) for audit and accountability.

---

### 3) `com.motorph.repository`
_Interfaces defining contracts for data persistence._

- **`EmployeeRepository`**: Read/write operations for employee master data.
- **`PayslipRepository`**: Stores generated payslips (supports historical retrieval for Monthly Payroll Summary and reporting).
- **`TimeEntryRepository`**: Stores and queries daily attendance logs used to compute timecards and hours.
- **`UserAccountRepository`**: Stores and retrieves user accounts for authentication and access control.
- **`HolidayRepository`** *(New)*: Retrieves holiday reference data used for holiday tagging and pay eligibility.
- **`AuditRepository`**: Stores and retrieves audit log records (`AuditLogEntry`) used for security monitoring and traceability.

> If your persistence design treats timecards as first-class stored records (separate from raw time entries), add:
> - **`TimecardRepository`**: Stores and retrieves `Timecard` aggregates for approval workflows and payroll processing.

---

### 4) `com.motorph.repository.csv`
_CSV-based implementations responsible for file I/O and storage paths._

- **`CsvEmployeeRepository`**: CSV persistence for employee profiles.
- **`CsvPayslipRepository`**: CSV persistence for generated payslips (final payroll outputs).
- **`CsvTimeEntryRepository`**: CSV persistence for daily time logs.
- **`CsvUserAccountRepository`**: CSV persistence for user credentials and role assignments.
- **`CsvHolidayRepository`** *(New)*: Loads holiday reference data from the holiday calendar CSV.
- **`CsvAuditRepository`**: CSV persistence for audit logs (failed logins, approvals, payroll actions).
- **`DataPaths`** *(Updated)*: Centralized configuration of CSV file paths, including:
  - **Master Data**
    - `data_Employee.csv`
    - `data_Login.csv` (user accounts)
    - `data_HolidayCalendar.csv`
    - `data_Supervisor.csv` (if used for manager/team mapping)
  - **Records / Outputs**
    - `records_dtr/` (per-employee DTR logs)
    - `records_payroll/` (per-employee payroll run records)
    - `records_payslips/` (generated payslip outputs)
  - **Change Logs**
    - `changeLogs_DTR.csv`
    - `changeLogs_EmpDataChangeLogs.csv`
    - `changeLogs_Payroll.csv`
  - **Compliance Tables (Government Reference Tables)**
    - `gov_SSS_Table.csv`
    - `gov_Philhealth_Table.csv`
    - `gov_Pagibig_Table.csv`
    - `gov_Tax_Table.csv`

> Note: Government tables should be treated as read-only reference datasets during runtime.

---

### 5) `com.motorph.service`
_Business logic layer for compliance, security, payroll computation, approvals, and reporting._

- **`AuthService`**: Authenticates users and performs lockout checks. Logs failed attempts via `AuditRepository` to mitigate unauthorized access risks.
- **`AccessControlService`** *(New)*: RBAC “gatekeeper” that enforces role permissions before allowing access to restricted dashboards/features.
- **`EmployeeService`**: Manages `EmployeeProfile` updates (salary adjustments, position/department changes) and logs sensitive changes when applicable.
- **`TimeEntryService`**: Validates and processes time entries and prepares them for aggregation into a timecard.
- **`TimecardApprovalService`**: Handles submit/approve/reject flows for timecards (ties into `ApprovalStatus`) and logs approval actions to audit when applicable.
- **`TimeCardService`** *(Renamed/Scoped)*: Validates time logs and cross-references `HolidayRepository` to tag holidays and compute correct hour classifications (regular/overtime/holiday).
- **`PayrollService`** *(Core)*: Orchestrates payroll processing flow (approved timecard → gross → deductions → net → payslip persistence). Delegates deduction calculations to a strategy implementation.
- **`ReportGenerationService`** *(New)*: Aggregates payslip data to generate the **Monthly Payroll Summary Matrix** (e.g., total SSS and total tax per department).
- **`UserMaintenanceService`**: IT admin operations such as unlocking accounts and resetting passwords (with audit logging for security-relevant actions).

---

### 6) `com.motorph.service.strategy`
_Compliance layer using the Strategy Pattern to support regulatory updates (Open/Closed Principle)._

- **`DeductionStrategy`**: Interface defining deduction computation contracts:
  - `calculateSSS()`
  - `calculatePhilHealth()`
  - `calculatePagibig()`
  - `calculateTax()`
- **`DeductionStrategy2025`**: Concrete strategy that applies the current reference tables (SSS Circular 2024-006-derived table, PhilHealth, Pag-IBIG, and withholding tax rules) to ensure compliant and maintainable payroll deductions.

---

### 7) `com.motorph.ui.swing`
_Swing UI layer aligned with system stakeholders and RBAC routing._

- **`SwingApp`**: Application entry point. Initializes services and injects `DeductionStrategy2025` into payroll processing.
- **`LoginView`**: Secure login screen calling `AuthService`.
- **`HomeView`** *(Router)*: Redirects users to the correct dashboard based on `Role`.
- **`EmployeeDashboardView`** *(New)*: Employee self-service for Time In/Out and viewing personal payslips.
- **`ManagerDashboardView`**: Timecard review and approval (shows `ApprovalStatus.PENDING`; allows Approve/Reject).
- **`HrDashboardView`**: HR management view for editing employee profile data.
- **`ItDashboardView`**: IT tools for account maintenance via `UserMaintenanceService` (unlock/reset).
- **`PayrollDashboardView`**: Payroll processing and reporting dashboard; triggers payslip generation and opens Summary Report view/output.
- **`PayView`**: Payslip presentation view matching the payslip structure (Earnings/Benefits/Deductions).
- **`ProfileView`**: Employee profile view (read-only for employees; editable for HR).
