# MO-IT110 MotorPH Payroll and Employee Management System (OOP) | Group 11

Java payroll and employee management prototype for **MO-IT110 (Object-oriented Programming)**.  
Architecture uses **Domain + Repository + Service + Ops (use-case layer) + Swing UI**, with **CSV-backed persistence** under the `/data` folder.

---

## Members
- Juan Carlos Manalastas
- James Lynelle Ongo
- Jason Bryan Tan
- Sean Gil Quiben

---

## Architectural Layout

### Layering
- **Domain** (`com.motorph.domain.*`)  
  Core business objects and enums used across the system.
- **Repository** (`com.motorph.repository.*`, `com.motorph.repository.csv.*`)  
  Persistence contracts and CSV implementations (file I/O).
- **Service** (`com.motorph.service.*`)  
  Business rules (payroll computation, attendance, leave usage, auth).
- **Ops (Use-case layer)** (`com.motorph.ops.*`)  
  Feature-based operations that coordinate services and repositories.  
  This keeps UI code thin and centralizes workflows per module.
- **UI** (`com.motorph.ui.swing.*`, `com.motorph.ui.resources.*`)  
  Swing screens and panels with role-based routing.

### Core Behaviors Implemented
- **Role-based access control** via `Role` and UI routing logic.
- **Semi-monthly pay periods** (1–15, 16–end-of-month) via `PayPeriod`.
- **DTR approval gate before payroll run** via `ApprovalStatus` + payroll approval tracking.
- **Payslip snapshots** stored per employee per pay period in CSV.
- **Statutory deductions** computed via a strategy abstraction (`DeductionStrategy`).

---

## Package and Class Overview

## 1) `com.motorph.domain.enums`
Enums used for consistency and workflow state.
- `Role` - Defines roles (EMPLOYEE, HR, IT, MANAGER, PAYROLL)
- `ApprovalStatus` - Workflow state (PENDING, APPROVED, REJECTED)
- `LeaveType` - Leave classification
- `LeaveStatus` - Leave workflow state

---

## 2) `com.motorph.domain.models`
Domain entities and value objects.
- `BaseEntity` - Shared base fields for entities that need IDs
- `Employee` - Master employee profile (personal and employment fields)
- `User` - Login identity (username, password value, role, lock status)
- `PayPeriod` - Immutable value object for pay period boundaries and period keys
- `PayPeriodFactory` - Helpers for generating pay periods
- `TimeEntry` - Daily attendance log (date, time in, time out)
- `Timecard` - Timecard wrapper (used as a unit of time tracking)
- `Payslip` - Payroll result snapshot (earnings, deductions, net pay, metadata)
- `LeaveRequest` - Leave request record (date and time range)
- `LeaveCredits` - Leave credit balances and tracking fields
- `LogEntry` - System log entry model used by logging persistence

---

## 3) `com.motorph.repository`
Persistence contracts (interfaces). These define WHAT can be stored or retrieved.
- `EmployeeRepository` - Employee master data access
- `UserRepository` - User credential and lock status access
- `TimeEntryRepository` - Attendance/DTR access
- `PayslipRepository` - Payslip persistence and retrieval
- `PayrollApprovalRepository` - DTR approval and payroll approval tracking per period
- `AuditRepository` - Audit-style change logging
- `LogRepository` - System log persistence
- `LeaveRepository` - Leave request persistence and leave usage computation
- `LeaveCreditsRepository` - Leave credits persistence
- `PayrollRecordRepository` - Contract placeholder for payroll record tracking (not required if using `PayrollApprovalRepository`)
- `TeamRepository` - Contract placeholder for supervisor-team mapping

---

## 4) `com.motorph.repository.csv`
CSV implementations responsible for file I/O and naming conventions.
- `DataPaths` - Centralized file and folder paths, plus defaults
- `AbstractCsvRepository` - Shared CSV helpers (read/write utilities)
- `CsvEmployeeRepository` - Employee CSV implementation
- `CsvUserRepository` - User CSV implementation (credentials, roles, lock status)
- `CsvTimeRepository` - DTR repository using `records_dtr_{empId}.csv`
- `CsvPayslipRepository` - Payslip snapshots using `records_payslips_{empId}_{periodKey}.csv`
- `CsvPayrollApprovalRepository` - Approval tracker using `records_payroll_{empId}.csv`
- `CsvLeaveRepository` - Leave repository using `records_leave_{empId}.csv`
- `CsvLeaveCreditsRepository` - Leave credits CSV implementation
- `CsvAuditRepository` - Audit log persistence
- `CsvLogRepository` - System log persistence

---

## 5) `com.motorph.service`
Business logic layer.
- `AuthService` - Authentication and lockout logic
- `EmployeeService` - Employee caching, supervisor checks, role inference helpers
- `TimeService` - Time-in/time-out rules and workday filtering
- `PayrollService` - Payroll computation and payslip generation workflow
- `LeaveService` - Leave-hours computation rules for payroll usage
- `LeaveCreditsService` - Remaining credits and year-to-date computations
- `LogService` - Writes system action logs through `LogRepository`

---

## 6) `com.motorph.service.strategy`
Strategy abstraction for deductions.
- `DeductionStrategy` - Deduction computation contract
- `PayDeductionStrategy` - Concrete implementation using the `/data/gov_*.csv` reference tables

---

## 7) `com.motorph.ops`
Use-case boundary layer (feature-based module operations).  
Each module exposes an interface and a coordinating implementation.
- `ops.auth`
  - `AuthOps`, `AuthOpsImpl` - Login and session-oriented flows
- `ops.time`
  - `TimeOps`, `TimeOpsImpl` - Employee time-in/time-out flows and attendance operations
- `ops.leave`
  - `LeaveOps`, `LeaveOpsImpl` - Leave requests and credit usage flows
- `ops.approval`
  - `DtrApprovalOps`, `DtrApprovalOpsImpl` - Approve/reject DTR per pay period
- `ops.payroll`
  - `PayrollOps`, `PayrollOpsImpl` - Payroll execution (single employee or batch), period resolution
  - `PayrollRunResult` - Batch processing result model
- `ops.payslip`
  - `PayslipOps`, `PayslipOpsImpl` - Payslip retrieval (latest or by period)
- `ops.supervisor`
  - `SupervisorOps`, `SupervisorOpsImpl` - Supervisor-side summaries and views
  - `SupervisorDtrSummary` - Supervisor summary model
- `ops.hr`
  - `HROps`, `HROpsImpl` - Employee create, update, archive flows
- `ops.it`
  - `ItOps`, `ItOpsImpl` - Password resets and lock/unlock operations

---

## 8) `com.motorph.ui.swing` and `com.motorph.ui.resources`
Swing UI screens and assets.
- `LoginView` - UI entry point (contains `main`)
- `MainDashboard` - Role-based routing and sidebar visibility
- `BasePanel` - Base UI panel structure
- `HomePanel` - Employee self-service (profile, time logs, payslip access)
- `SupervisorPanel` - Supervisor functions and summaries
- `HrPanel` - HR employee management UI
- `PayrollPanel` - Payroll processing UI
- `LeavePanel` - Leave request UI
- `ui.resources/images/*` - Logo and UI images

---

## 9) `com.motorph.utils`
- `PasswordUtil` - SHA-256 hashing helper (utility class)

---

## 10) `com.motorph.test`
Console-based test runners and utilities.
- `BackEndTester` - End-to-end backend test runner (pay periods, DTR seeding, payslip generation)
- `BackEndPayrollTester` - Payroll-focused runner
- `BackEndAuthTester` - Auth-focused runner
- `BackEndItOpsTester` - IT ops runner (lock/unlock, password reset)
- `PasswordMigrator` - CSV password migration helper (creates a secure output CSV)

---

## Data Folder Layout (as defined in `DataPaths`)
Expected structure under `./data/`:

- Core files
  - `data_Employee.csv`
  - `data_Legacy_LogIn.csv`
  - `data_LeaveCredits.csv`
  - `data_HolidayCalendar.csv`
- Reference tables
  - `gov_SSS_Table.csv`
  - `gov_Philhealth_Table.csv`
  - `gov_Pagibig_Table.csv`
  - `gov_Tax_Table.csv`
- Logs
  - `changeLogs_records.csv`
  - `changeLogs_DTR.csv`
  - `changeLogs_EmpDataChangeLogs.csv`
  - `changeLogs_Payroll.csv`
  - `system_logs.csv`
- Record folders
  - `records_dtr/` (creates `records_dtr_{empId}.csv`)
  - `records_payroll/` (creates `records_payroll_{empId}.csv`)
  - `records_payslips/` (creates `records_payslips_{empId}_{periodKey}.csv`)
  - `records_leave/` (creates `records_leave_{empId}.csv`)

---

## How to Run
- **Run Swing UI**: `com.motorph.ui.swing.LoginView` (has `main`)
- **Run backend tests**: `com.motorph.test.BackEndTester`

---

## Notes
- File paths and folder names are centralized in `com.motorph.repository.csv.DataPaths`.
- Government reference tables (`gov_*.csv`) are intended as read-only inputs during runtime.
