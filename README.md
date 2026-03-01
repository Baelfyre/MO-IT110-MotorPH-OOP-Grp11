# MO-IT110 MotorPH Payroll and Employee Management System (OOP) | Group 11

Java payroll and employee management prototype for **MO-IT110 (Object-oriented Programming)**.

This project is being refactored from a legacy implementation into a cleaner OOP structure using:

- Domain
- Repository
- Service
- Ops (use-case layer)
- Swing UI

Persistence is **CSV-backed** and stored under the `/data` folder.

---

## Members

- Juan Carlos Manalastas
- James Lynelle Ongo
- Jason Bryan Tan
- Sean Gil Quiben

---

## Project Overview

The MotorPH Payroll and Employee Management System is a Java-based prototype designed to support core payroll and employee workflows such as:

- employee profile management
- authentication and account lock control
- daily time record (DTR) handling
- leave filing and leave credit tracking
- payroll computation
- payslip generation
- role-based access control

The refactoring focuses on separating business logic from UI code, improving maintainability, and aligning the project with core OOP principles such as encapsulation, abstraction, modularity, and separation of concerns.

---

## Architecture

### Layered Structure

- **Domain** (`com.motorph.domain.*`)  
  Core business objects, value objects, and enums shared across the system.

- **Repository** (`com.motorph.repository.*`, `com.motorph.repository.csv.*`)  
  Persistence contracts and CSV-based implementations for file storage and retrieval.

- **Service** (`com.motorph.service.*`)  
  Business rules and validation logic for payroll, attendance, leave, and authentication.

- **Ops / Use-case Layer** (`com.motorph.ops.*`)  
  Module-based workflows that coordinate repositories and services. This keeps UI classes thin and places process logic inside dedicated use-case handlers.

- **UI** (`com.motorph.ui.swing.*`, `com.motorph.ui.resources.*`)  
  Swing screens, panels, and assets for user interaction and role-based navigation.

---

## Refactoring Goals

- move business rules out of UI classes
- keep Swing screens focused on display and user interaction
- centralize workflow logic per feature through Ops classes
- isolate CSV file handling inside repository implementations
- reduce duplicated logic across modules
- make the codebase easier to test, extend, and maintain

---

## Core Behaviors Implemented

- Role-based access control using `Role` and UI routing
- Semi-monthly pay periods through `PayPeriod`
- DTR approval gate before payroll execution
- Payslip snapshot storage per employee per pay period
- Statutory deductions through `DeductionStrategy`
- CSV-backed persistence for records and reference tables
- Role-specific operations for HR, Payroll, Supervisor, IT, and Employee workflows

---

## Package Overview

### 1) `com.motorph.domain.enums`

Enums used for consistency and workflow state.

- `Role` - Defines system roles (`EMPLOYEE`, `HR`, `IT`, `MANAGER`, `PAYROLL`)
- `ApprovalStatus` - Defines workflow status (`PENDING`, `APPROVED`, `REJECTED`)
- `LeaveType` - Defines leave classifications
- `LeaveStatus` - Defines leave request workflow state

---

### 2) `com.motorph.domain.models`

Core domain entities and value objects.

- `Employee` - Master employee profile with personal and employment details
- `User` - Login identity with username, password value, role, and lock status
- `PayPeriod` - Immutable value object for pay period boundaries and keys
- `PayPeriodFactory` - Helper for generating pay periods
- `TimeEntry` - Daily attendance log
- `Timecard` - Wrapper object used for time tracking operations
- `Payslip` - Payroll result snapshot containing earnings, deductions, and net pay
- `LeaveRequest` - Leave request record with date and time range
- `LeaveCredits` - Leave balance and usage tracking model
- `LogEntry` - System log entry model

---

### 3) `com.motorph.repository`

Persistence contracts that define what data can be stored or retrieved.

- `EmployeeRepository`
- `UserRepository`
- `TimeEntryRepository`
- `PayslipRepository`
- `PayrollApprovalRepository`
- `AuditRepository`
- `LogRepository`
- `LeaveRepository`
- `LeaveCreditsRepository`

---

### 4) `com.motorph.repository.csv`

CSV-based repository implementations responsible for file I/O and naming conventions.

- `DataPaths` - Centralized paths, folder names, and default file references
- `AbstractCsvRepository` - Shared CSV helper methods
- `CsvEmployeeRepository`
- `CsvUserRepository`
- `CsvTimeRepository`
- `CsvPayslipRepository`
- `CsvPayrollApprovalRepository`
- `CsvLeaveRepository`
- `CsvLeaveCreditsRepository`
- `CsvAuditRepository`
- `CsvLogRepository`

#### File Naming Pattern

- DTR records: `records_dtr_{empId}.csv`
- Payroll approval records: `records_payroll_{empId}.csv`
- Payslip snapshots: `records_payslips_{empId}_{periodKey}.csv`
- Leave records: `records_leave_{empId}.csv`

---

### 5) `com.motorph.service`

Business logic layer.

- `AuthService` - Authentication, password validation, and lockout handling
- `EmployeeService` - Employee lookup, caching, supervisor checks, and role inference helpers
- `TimeService` - Time-in, time-out, workday filtering, and attendance rules
- `PayrollService` - Payroll computation and payslip generation
- `LeaveService` - Leave-hour usage and payroll-related leave calculations
- `LeaveCreditsService` - Leave balance, remaining credits, and year-to-date tracking
- `LogService` - Writes system logs through `LogRepository`

---

### 6) `com.motorph.service.strategy`

Strategy-based deduction computation.

- `DeductionStrategy` - Contract for deduction computation
- `PayDeductionStrategy` - Concrete implementation using government reference tables in `/data`

---

### 7) `com.motorph.ops`

Use-case layer organized by feature or module. Each module exposes an interface plus a coordinating implementation.

#### Auth
- `AuthOps`
- `AuthOpsImpl`

#### Time
- `TimeOps`
- `TimeOpsImpl`

#### Leave
- `LeaveOps`
- `LeaveOpsImpl`

#### Approval
- `DtrApprovalOps`
- `DtrApprovalOpsImpl`

#### Payroll
- `PayrollOps`
- `PayrollOpsImpl`
- `PayrollRunResult`

#### Payslip
- `PayslipOps`
- `PayslipOpsImpl`

#### Supervisor
- `SupervisorOps`
- `SupervisorOpsImpl`
- `SupervisorDtrSummary`

#### HR
- `HROps`
- `HROpsImpl`

#### IT
- `ItOps`
- `ItOpsImpl`

---

### 8) `com.motorph.ui.swing` and `com.motorph.ui.resources`

Swing-based screens, panels, and static assets.

- `LoginView` - System entry point and login screen (`main`)
- `MainDashboard` - Main role-based container and navigation shell
- `BasePanel` - Shared base structure for reusable UI panels
- `HomePanel` - Employee self-service panel
- `SupervisorPanel` - Supervisor functions and summaries
- `HrPanel` - HR employee management interface
- `PayrollPanel` - Payroll processing interface
- `LeavePanel` - Leave request interface
- `ui.resources/images/*` - Logos and image assets

---

### 9) `com.motorph.utils`

- `PasswordUtil` - SHA-256 hashing helper

---

### 10) `com.motorph.test`

Console-based backend test runners and migration utilities.

- `BackEndTester` - End-to-end backend workflow testing
- `BackEndPayrollTester` - Payroll-focused testing
- `BackEndAuthTester` - Authentication-focused testing
- `BackEndItOpsTester` - IT operations testing
- `PasswordMigrator` - CSV password migration utility for secure output generation

---

## Project Structure

```text
com.motorph
├── domain
│   ├── enums
│   └── models
├── repository
│   └── csv
├── service
│   └── strategy
├── ops
│   ├── approval
│   ├── auth
│   ├── hr
│   ├── it
│   ├── leave
│   ├── payroll
│   ├── payslip
│   ├── supervisor
│   └── time
├── ui
│   ├── resources
│   └── swing
├── utils
└── test
