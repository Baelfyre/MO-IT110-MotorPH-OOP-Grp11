# MO-IT110 MotorPH Payroll and Employee Management System (OOP) | Group 11

Java-based payroll and employee management prototype for **MO-IT110: Object-oriented Programming**.

This project refactors a legacy implementation into a cleaner object-oriented design using a layered architecture:

- Domain
- Repository
- Service
- Ops / Use-case layer
- Swing UI

The system uses **CSV-backed persistence** under the `/data` folder and focuses on maintainability, validation, role-based workflows, and clearer separation of concerns.

---

## Members

- Juan Carlos Manalastas
- James Lynelle Ongo
- Jason Bryan Tan
- Sean Gil Quiben

---

## Project Overview

The MotorPH Payroll and Employee Management System is a Java desktop prototype built to support core employee and payroll workflows for a growing company environment. The current implementation covers:

- authentication with account lock status handling
- role-based access control using permissions per role
- employee record creation, update, delete, and archived filtering
- automatic login provisioning for newly created employees
- employee self-service for profile update, attendance, leave, and payslip viewing
- daily time record logging and approval-aware payroll processing
- leave filing with balance tracking and unpaid leave fallback handling
- semi-monthly payroll computation and payslip generation
- system logging and audit-related CSV records
- CSV-based storage for employee, login, DTR, leave, payroll, payslip, log, and reference data

The refactoring effort keeps UI classes focused on presentation while moving business rules and workflow coordination into service and ops layers.

---

## Current Milestone 2 Implementation Highlights

The current source code already reflects several milestone-aligned improvements and refactoring changes:

- Introduced an **abstract `Employee`** base model with concrete subclasses:
  - `RegularEmployee`
  - `ProbationaryEmployee`
- Added supporting domain models such as:
  - `EmployeeDetails`
  - `Position`
  - `Compensation`
  - `PayPeriod`
  - `Payslip`
  - `LeaveRequest`
  - `LeaveCredits`
  - `LogEntry`
  - `AddressReference`
- Added **`BaseEntity`** as a shared abstract domain base for reusable entity behavior.
- Strengthened **encapsulation** by keeping sensitive employee, compensation, and login data inside domain objects with getters/setters and validation.
- Strengthened **abstraction and polymorphism** through interfaces and overrides in:
  - `DeductionStrategy`
  - `AuthOps`, `TimeOps`, `LeaveOps`, `PayrollOps`, `PayslipOps`, `SupervisorOps`, `HROps`, `ItOps`
  - `RegularEmployee` and `ProbationaryEmployee`
- Centralized validation logic through utility and service classes instead of leaving checks inside the UI.
- Added backend RBAC checks in critical HR, Payroll, IT, and Payslip workflows.
- Improved module separation so repositories handle CSV persistence, services handle business rules, ops handle use cases, and Swing panels handle presentation.

---

## Recent Functional Updates Reflected in the Codebase

### HR Module
- Employee CRUD is routed through `HROps` and `HROpsImpl`.
- New employee creation auto-generates a linked login record.
- Employee validation checks include age, phone format, government IDs, salary floor, and allowed allowance values.
- HR screens support search, refresh, archived employee filtering, detail viewing, and system log viewing.
- Profile update support is available through `UpdateProfile` and address reference lookup utilities.

### Timekeeping and Attendance
- Attendance actions are handled through `TimeService` and `TimeOps`.
- Timekeeping screens now show current period, DTR status, and worked hours for the current day.
- The service layer includes short-duration checks for suspiciously short worked time after time-out.
- Attendance history is shown through filtered table views.

### Leave Management
- Leave filing is handled through `LeaveOps`, `LeaveService`, and `LeaveCreditsService`.
- Leave validation is centralized in the service layer.
- Leave hours used, stored credits, and remaining year-to-date balance are surfaced in the UI.
- The leave request flow includes handling for cases where paid leave credits are insufficient.
- Leave history and supervisor comments can be reviewed from the leave panel.

### Payroll and Payslips
- Payroll processing is handled through `PayrollOps` and `PayrollService`.
- Payroll follows a **semi-monthly pay period** model.
- DTR approval status is checked before payroll can be processed.
- Batch payroll processing is available from the payroll panel.
- Payroll queue rows are represented using `PayrollQueueItem`.
- Payslip history, filtering, and preview are available through `PayslipPanel` and `PayslipOps`.
- Payslip viewing includes access checks so employees can only view their own records unless authorized.

### IT and Security
- IT account maintenance is handled through `ItOps`.
- Lock, unlock, default password reset, and custom password reset actions are implemented.
- Backend permission checks prevent unauthorized account maintenance actions.
- Self-lockout prevention is enforced in IT lock-status actions.

### Logging and Testing
- `LogService` records system actions across modules.
- CSV-backed audit and system log repositories are part of the current implementation.
- Backend test runners are included for general, payroll, authentication, IT, and HR smoke testing.

---

## Architecture

### Layered Structure

- **Domain** (`com.motorph.domain.*`)  
  Core business entities, value objects, shared enums, and inheritance-based models.

- **Repository** (`com.motorph.repository.*`, `com.motorph.repository.csv.*`)  
  Persistence contracts and CSV-backed implementations for reading and writing project data.

- **Service** (`com.motorph.service.*`)  
  Business rules for authentication, employee lookup, attendance, leave, payroll, and logs.

- **Ops / Use-case Layer** (`com.motorph.ops.*`)  
  Workflow-oriented modules that coordinate repositories and services per feature.

- **UI** (`com.motorph.ui.swing.*`, `com.motorph.ui.resources.*`)  
  Swing forms, panels, dialogs, and assets for role-based user interaction.

This structure supports separation of concerns by keeping persistence, business logic, process flow, and presentation responsibilities in their own layers.

---

## OOP Principles Demonstrated

### Encapsulation
- Employee personal details, compensation, and login data are stored inside domain objects.
- Validation rules are routed through setters, utility methods, and service methods instead of being scattered across forms.

### Abstraction
- Core behaviors are expressed through interfaces such as `DeductionStrategy`, `PayrollOps`, `LeaveOps`, `HROps`, and `ItOps`.
- Higher-level modules work with contracts instead of hardcoding implementation details into the UI.

### Inheritance
- `Employee` is modeled as an abstract parent class.
- `RegularEmployee` and `ProbationaryEmployee` override leave-credit behavior.
- `BaseEntity` is available as a shared abstract base for reusable entity structure.

### Polymorphism
- Ops interfaces are implemented by dedicated classes such as `PayrollOpsImpl`, `LeaveOpsImpl`, `HROpsImpl`, and `ItOpsImpl`.
- `DeductionStrategy` allows deduction logic to be swapped without changing payroll orchestration code.
- Employee subclasses provide different leave-credit behavior through overridden methods.

---

## Core Packages

### 1) `com.motorph.domain.enums`

Enums used for workflow state, employment type, role control, and system consistency.

- `ApprovalStatus`
- `EmploymentStatus`
- `JobPosition`
- `LeaveStatus`
- `LeaveType`
- `Role`

### 2) `com.motorph.domain.models`

Core domain entities and value objects.

- `AddressReference`
- `BaseEntity`
- `Compensation`
- `Employee`
- `EmployeeDetails`
- `LeaveCredits`
- `LeaveRequest`
- `LogEntry`
- `PayPeriod`
- `PayPeriodFactory`
- `Payslip`
- `Position`
- `ProbationaryEmployee`
- `RegularEmployee`
- `TimeEntry`
- `User`

### 3) `com.motorph.repository`

Repository contracts for data access.

- `AuditRepository`
- `EmployeeRepository`
- `LeaveCreditsRepository`
- `LeaveRepository`
- `LogRepository`
- `PayrollApprovalRepository`
- `PayrollRecordRepository`
- `PayslipRepository`
- `TimeEntryRepository`
- `UserRepository`

### 4) `com.motorph.repository.csv`

CSV-backed persistence implementations and shared file-path management.

- `AbstractCsvRepository`
- `CsvAddressReferenceRepository`
- `CsvAuditRepository`
- `CsvEmployeeRepository`
- `CsvLeaveCreditsRepository`
- `CsvLeaveRepository`
- `CsvLogRepository`
- `CsvPayrollApprovalRepository`
- `CsvPayslipRepository`
- `CsvTimeRepository`
- `CsvUserRepository`
- `DataPaths`

### 5) `com.motorph.service`

Business logic and validation-related services.

- `AuthService`
- `EmployeeService`
- `LeaveCreditsService`
- `LeaveService`
- `LogService`
- `PayrollService`
- `TimeService`

### 6) `com.motorph.service.strategy`

Strategy-based payroll deduction logic.

- `DeductionStrategy`
- `PayDeductionStrategy`

### 7) `com.motorph.ops`

Use-case layer grouped by module.

- `approval`  
  `DtrApprovalOps`, `DtrApprovalOpsImpl`
- `auth`  
  `AuthOps`, `AuthOpsImpl`
- `hr`  
  `HROps`, `HROpsImpl`
- `it`  
  `ItOps`, `ItOpsImpl`
- `leave`  
  `LeaveOps`, `LeaveOpsImpl`
- `payroll`  
  `PayrollOps`, `PayrollOpsImpl`, `PayrollQueueItem`, `PayrollRunResult`
- `payslip`  
  `PayslipOps`, `PayslipOpsImpl`
- `supervisor`  
  `SupervisorDtrSummary`, `SupervisorOps`, `SupervisorOpsImpl`
- `time`  
  `TimeOps`, `TimeOpsImpl`

### 8) `com.motorph.ui.swing`

Swing-based screens and panels.

- `EmployeeFormPanel`
- `HomePanel`
- `HrPanel`
- `ITPanel`
- `LeavePanel`
- `LoginPanel`
- `MainDashboard`
- `PayrollPanel`
- `PayslipPanel`
- `SelfServicePanel`
- `SupervisorPanel`
- `SwingForm`
- `TimekeepingPanel`
- `UiDialogs`
- `UpdateProfile`
- helper classes under `UiHelper`

### 9) `com.motorph.utils`

Shared utility helpers.

- `AddressFormatter`
- `AddressParser`
- `InputRestrictionUtil`
- `PasswordUtil`
- `ValidationUtil`

### 10) `com.motorph.test`

Console-based test runners and smoke tests.

- `BackEndAuthTester`
- `BackEndHROpsSmokeTester`
- `BackEndItOpsTester`
- `BackEndPayrollTester`
- `BackEndTester`

---

## Data and Persistence

Project data is stored in CSV files under the `/data` folder. The current path registry in `DataPaths` includes:

- employee master data
- login credentials data
- DTR records folder
- payroll approval records folder
- payslip records folder
- leave records folder
- leave credits data
- address reference data
- holiday calendar data
- government deduction reference tables
- audit and system log files

This keeps the project lightweight while still supporting modular file-based persistence.

---

## Application Entry Point

The application boots from:

```text
com.motorph.SwingApp
```

`SwingApp` wires repositories, services, ops modules, and the Swing UI composition root before launching `LoginPanel`.

---

## Current UI Scope

The current Swing implementation includes screens or panels for:

- login
- main dashboard
- employee self-service
- attendance / timekeeping
- leave requests
- payslip viewing
- payroll management
- HR employee management
- supervisor actions
- IT account maintenance
- profile update
- system logs and supporting dialogs

---

## Project Structure

```text
com.motorph
├── SwingApp.java
├── domain
│   ├── enums
│   └── models
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
├── repository
│   └── csv
├── service
│   └── strategy
├── test
├── ui
│   ├── resources
│   └── swing
└── utils
```

---

**Java Swing payroll and employee management system for MotorPH using layered OOP architecture, CSV persistence, RBAC, attendance, leave, payroll, and payslip workflows.**
