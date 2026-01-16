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
- **ApprovalStatus**: Standard values for approval workflow states (e.g., Pending, Approved, Rejected) used by timecards/payroll actions.
- **Role**: Defines system roles (e.g., Employee, HR, IT, Manager, Payroll) used for access control and UI routing.
- **TicketCategory**: Groups tickets by type (e.g., Payroll, HR, IT) to route/organize issues properly.
- **TicketStatus**: Tracks the lifecycle of a ticket (e.g., Open, In Progress, Resolved, Closed).

### `com.motorph.domain.models` (Core domain objects)
- **AuditLogEntry**: Represents a single audit record (who did what, when, and details) for traceability.
- **DtrChangeLogEntry**: Captures edits made to DTR/time records (what changed, who changed it, and when).
- **EmployeeProfile**: Stores employee master data (employee number, name, birthday, and other profile fields).
- **PayPeriod**: Represents a payroll cut-off window (start/end dates) used to group time entries and compute pay.
- **Payslip**: Stores computed payroll results for an employee (hours, gross, deductions, net) for a pay period/week.
- **Ticket**: Stores ticket details (ID, creator, category, status, description, timestamps, assignment).
- **TimeEntry**: Represents a single log-in/log-out record or daily work record used to compute hours.
- **Timecard**: Aggregates time entries for a week/pay period and is the unit typically submitted for approval.
- **UserAccount**: Holds login/access information (username, password hash, role, lock status, etc.).

### `com.motorph.repository` (Repository interfaces: contracts)
- **AuditRepository**: Defines how audit logs are saved/retrieved (interface only, implementation elsewhere).
- **EmployeeRepository**: Defines CRUD/read operations for employee records.
- **PayslipRepository**: Defines how computed payslips are stored and retrieved.
- **TeamRepository**: Defines how team/department mappings are stored/retrieved (often for assignment/routing).
- **TicketRepository**: Defines how tickets are created, updated, and queried.
- **TimeEntryRepository**: Defines how time entries are stored and queried.
- **UserAccountRepository**: Defines how user accounts are stored, retrieved, and authenticated against.

### `com.motorph.repository.csv` (CSV persistence implementations)
- **CsvAuditRepository**: CSV-based implementation of `AuditRepository`.
- **CsvEmployeeRepository**: CSV-based implementation of `EmployeeRepository`.
- **CsvPayslipRepository**: CSV-based implementation of `PayslipRepository`.
- **CsvTeamRepository**: CSV-based implementation of `TeamRepository`.
- **CsvTicketRepository**: CSV-based implementation of `TicketRepository`.
- **CsvTimeEntryRepository**: CSV-based implementation of `TimeEntryRepository`.
- **CsvUserAccountRepository**: CSV-based implementation of `UserAccountRepository`.
- **DataPaths**: Central place for CSV file paths/constants so file locations are not hardcoded across classes.

### `com.motorph.service` (Business logic layer)
- **AccessControlService**: Enforces permissions (what each role can view/do) across features.
- **AuthService**: Handles login validation, user authentication, and lockout checks.
- **EmployeeService**: Employee-related operations (fetch profiles, formatting display info, basic validations).
- **PayrollService**: Core payroll computations (hours → gross → deductions → net) and payslip generation.
- **TicketIdGenerator**: Generates unique ticket IDs (often with sequence + formatting rules).
- **TicketService**: Ticketing business logic (create ticket, assign, update status, list/filter).
- **TimeEntryService**: Validates/processes time entries and prepares them for weekly/pay-period aggregation.
- **TimecardApprovalService**: Handles submit/approve/reject flows for timecards (ties into `ApprovalStatus`).
- **UserMaintenanceService**: Admin functions for accounts (create/update users, role changes, lock/unlock).

### `com.motorph.ui.console` (Console UI)
- **ConsoleApp**: Command-line runner for quick testing/demo of workflows without Swing.

### `com.motorph.ui.swing` (Swing UI)
- **SwingApp**: Main entry point/launcher for the Swing application and view navigation setup.
- **LoginView**: Login screen UI; calls `AuthService`.
- **HomeView**: General landing page after login; routes users to role-specific dashboards.
- **HrDashboardView**: HR-specific dashboard (employee-related features and HR actions).
- **ItDashboardView**: IT-specific dashboard (account maintenance, lockout toggles, system utilities).
- **ManagerDashboardView**: Manager dashboard (timecard review/approval, team oversight).
- **PayrollDashboardView**: Payroll dashboard (payslip generation, payroll run views, payroll review).
- **ProfileView**: Displays employee/user profile details (read-only or editable depending on role).
- **PayView**: Payroll computation/payslip viewing screen (hours, gross, deductions, net).
- **TicketsView**: Ticket list/details UI (create, view, filter, update status based on permissions).

---

## Notes
- Current implementation focuses on the system structure (classes and UI screens) as a foundation.
- Code snippets and feature implementations will be added incrementally as development progresses.
