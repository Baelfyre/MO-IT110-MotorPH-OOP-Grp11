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

## Package Overview

### `com.motorph.domain.enum` (Enums)
- **ApprovalStatus**: Standard values for approval workflow states (Pending, Approved, Rejected) used for timecard validation and payroll actions.
- **Role**: Defines system roles (Employee, HR, IT, Manager, Payroll) used for role-based access control and UI routing.
- **Permission** *(New)*: Fine-grained access rights (e.g., VIEW_REPORTS, RUN_PAYROLL, EDIT_EMPLOYEE, UNLOCK_USER, APPROVE_TIMECARD) enforced by `AccessControlService`.
- **TicketCategory**: Groups tickets by type (Payroll, HR, IT) to route/organize issues properly.
- **TicketStatus**: Tracks the lifecycle of a ticket (Open, In Progress, Resolved, Closed).

### `com.motorph.domain.models` (Core domain objects)
- **AuditLogEntry**: Represents a single audit record (who did what, when, and details) for traceability and security monitoring.
- **DtrChangeLogEntry**: Captures edits made to DTR/time records (what changed, who changed it, and when).
- **EmployeeProfile**: Master employee data record (employee number, name, birthday, salary base, department, and other profile fields). Used for department-based reporting.
- **PayPeriod**: Represents a payroll cut-off window (start/end dates) used to group time entries/timecards and compute pay.
- **Holiday** *(New)*: Stores holiday dates and type (Regular/Special). Used to automatically tag time entries and determine holiday pay eligibility.
- **TimeEntry**: Represents a single day’s log (time in/time out). Includes logic/flags for Workday vs Weekend and supports holiday tagging.
- **Timecard**: Aggregate root that groups a list of `TimeEntry` records for a pay period/week and is the unit submitted for approval.
- **Payslip** *(Refactored Composite)*: Stores computed payroll results for an employee for a pay period. Now composed of:
  - **Earnings** (Rate, Days Worked, Overtime)
  - **Benefits** (Rice, Clothing, Phone)
  - **Deductions** (SSS, PhilHealth, Pag-IBIG, Withholding Tax)
- **UserAccount**: Holds login/access information (username, password hash, role, lock status, etc.) and links to `EmployeeProfile`.
- **Ticket**: Stores ticket details (ID, creator, category, status, description, timestamps, assignment).

### `com.motorph.repository` (Repository interfaces: contracts)
- **AuditRepository**: Defines how audit logs are saved/retrieved.
- **EmployeeRepository**: Defines CRUD/read operations for employee master data.
- **PayslipRepository**: Defines how generated payslips are stored and retrieved (supports historical Monthly Payroll Summary reporting).
- **TimeEntryRepository**: Defines how daily time logs are stored and queried.
- **TimecardRepository** *(Recommended)*: Defines how timecards are stored, retrieved, and queried for approval workflows.
- **UserAccountRepository**: Defines how user accounts are stored, retrieved, and authenticated against.
- **HolidayRepository** *(New)*: Defines how holiday dates are retrieved to support holiday tagging and pay eligibility.
- **TeamRepository**: Defines how team/department mappings are stored/retrieved (often for assignment/routing).
- **TicketRepository**: Defines how tickets are created, updated, and queried.

### `com.motorph.repository.csv` (CSV persistence implementations)
- **CsvAuditRepository**: CSV-based implementation of `AuditRepository`.
- **CsvEmployeeRepository**: CSV-based implementation of `EmployeeRepository`.
- **CsvPayslipRepository**: CSV-based implementation of `PayslipRepository`.
- **CsvTimeEntryRepository**: CSV-based implementation of `TimeEntryRepository`.
- **CsvTimecardRepository** *(Recommended)*: CSV-based implementation of `TimecardRepository` if timecards are persisted separately from time entries.
- **CsvUserAccountRepository**: CSV-based implementation of `UserAccountRepository`.
- **CsvHolidayRepository** *(New)*: Loads holiday reference data (e.g., `Holidays_2025.csv`).
- **CsvTeamRepository**: CSV-based implementation of `TeamRepository`.
- **CsvTicketRepository**: CSV-based implementation of `TicketRepository`.
- **DataPaths** *(Updated)*: Central configuration for CSV file paths/constants so file locations are not hardcoded across classes. Includes compliance/reference tables, such as:
  - `SSS_Table_2025.csv`
  - `Philhealth_2025.csv`
  - `Tax_Table.csv`
  - `Pagibig_Table.csv` 
  - `Holidays_2025.csv`

### `com.motorph.service` (Business logic layer)
- **AuthService**: Handles login validation, user authentication, lockout checks, and failed-login audit logging.
- **AccessControlService**: Enforces permissions (what each role can view/do) across features using `Role` and `Permission`.
- **EmployeeService**: Employee-related operations (fetch/update profiles, validations, salary/department changes).
- **TimeEntryService**: Validates/processes time entries and prepares them for weekly/pay-period aggregation.
- **TimecardApprovalService**: Handles submit/approve/reject flows for timecards (ties into `ApprovalStatus`).
- **PayrollService**: Core payroll orchestration (approved timecards → gross → deductions → net) and payslip generation; delegates deductions to a strategy implementation.
- **ReportGenerationService** *(New)*: Generates reporting outputs such as the Monthly Payroll Summary Matrix (e.g., totals per department: SSS, tax, etc.) using persisted payslip data.
- **UserMaintenanceService**: Admin functions for accounts (create/update users, role changes, lock/unlock, password resets).
- **TicketIdGenerator**: Generates unique ticket IDs (often with sequence + formatting rules).
- **TicketService**: Ticketing business logic (create ticket, assign, update status, list/filter).

### `com.motorph.service.strategy` *(New: Compliance via Strategy Pattern)*
- **DeductionStrategy**: Interface defining deduction computations (SSS, PhilHealth, Pag-IBIG, Withholding Tax).
- **DeductionStrategy2025**: Concrete implementation that reads from the current compliance tables (SSS/PhilHealth/Tax/Pag-IBIG) to keep payroll rules compliant and maintainable.

### `com.motorph.ui.console` (Console UI)
- **ConsoleApp**: Command-line runner for quick testing/demo of workflows without Swing.

### `com.motorph.ui.swing` (Swing UI)
- **SwingApp**: Main entry point/launcher for the Swing application and dependency initialization (including deduction strategy injection).
- **LoginView**: Login screen UI; calls `AuthService`.
- **HomeView**: General landing page after login; routes users to role-specific dashboards.
- **EmployeeDashboardView** *(New)*: Employee Self-Service (Time In/Out, view own payslip, view profile).
- **HrDashboardView**: HR-specific dashboard (employee profile maintenance and HR actions).
- **ItDashboardView**: IT-specific dashboard (account maintenance, lockout toggles, system utilities).
- **ManagerDashboardView**: Manager dashboard (timecard review/approval, team oversight).
- **PayrollDashboardView**: Payroll dashboard (payslip generation, payroll runs, reporting access).
- **ProfileView**: Displays employee/user profile details (read-only or editable depending on role).
- **PayView**: Payslip viewing screen that matches the payslip structure (Earnings vs Benefits vs Deductions).
- **TicketsView**: Ticket list/details UI (create, view, filter, update status based on permissions).

## Notes
- Current implementation focuses on establishing a clean project structure (domain models, repositories, services, and UI screens) as the foundation.
- Compliance tables (SSS/PhilHealth/Tax/Pag-IBIG) are treated as reference datasets; payslips and summary reports are generated outputs derived from approved timecards.
- Features and validations will be implemented incrementally, with access control enforced centrally through `AccessControlService` to avoid duplicated security logic across UI screens.
