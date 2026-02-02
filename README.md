# MO-IT110-MotorPH-OOP-Grp11

MotorPH Payroll and Employee Management System (OOP) for **MO-IT110**.  
Built in Java using layered architecture (Domain, Repository, Service, UI) with CSV-backed persistence and role-based screens.

---

## Members
- Juan Carlos Manalastas
- James Lynelle Ongo
- Jason Bryan Tan
- Sean Gil Quiben

---
# MO-IT110-MotorPH-OOP-Grp11

MotorPH Payroll and Employee Management System (OOP) for **MO-IT110**.  
Built in Java using layered architecture (Domain, Repository, Service, UI) with CSV-backed persistence and role-based screens.

---

## Members
- Juan Carlos Manalastas
- James Lynelle Ongo
- Jason Bryan Tan
- Sean Gil Quiben

---

## Recalibrated Project Architecture - Package Overview

### 1) `com.motorph`
**Purpose:** Application entry point and bootstrap.

- **`SwingApp.java` (Main Class):** Primary entry point. Initializes repositories and services, then launches `LoginView`.

---

### 2) `com.motorph.domain.enums`
**Purpose:** System-wide constants to enforce data integrity and consistent workflows.

- **`Role.java` (Enum):** Defines system roles (`ADMIN`, `HR`, `SUPERVISOR`, `EMPLOYEE`) for role-based access control.
- **`LeaveStatus.java` (Enum):** Standardizes leave workflow states (`PENDING`, `APPROVED`, `REJECTED`).
- **`LeaveType.java` (Enum):** Defines leave categories (`SICK`, `VACATION`, `EMERGENCY`).

---

### 3) `com.motorph.domain.models`
**Purpose:** Rich domain models (with inheritance support) for payroll computation and compliance.

- **`BaseEntity.java` (Abstract Class):** Parent template defining shared `id` and the abstract `toCsvRow()` method for all records.
- **`Employee.java` (Class):** Master employee record storing personal details, salary base, and leave credits.
- **`User.java` (Class):** Stores credentials and assigned `Role` for authentication.
- **`LeaveRequest.java` (Class):** Tracks leave applications and approval status.
- **`TimeEntry.java` (Class):** Represents a single daily attendance log (clock-in/out).
- **`LogEntry.java` (Class):** Blueprint for unified audit trail (timestamp, user, action, details).
- **`Payslip.java` (Class):** Helper object to hold and display computed payroll results.

---

### 4) `com.motorph.repository` and `com.motorph.repository.csv`
**Purpose:** Persistence layer using abstraction to standardize CSV file I/O.

- **`AbstractCsvRepository.java` (Abstract Class):** Encapsulates core logic for reading and writing any CSV file.
- **`CsvEmployeeRepository.java` (Class):** Manages persistence for employee profiles.
- **`CsvUserRepository.java` (Class):** Handles login account data.
- **`CsvTimeRepository.java` (Class):** Stores and retrieves daily attendance logs.
- **`CsvLeaveRepository.java` (Class):** Manages leave request records.
- **`CsvLogRepository.java` (Class):** Handles the unified `changeLogs_records.csv` file.
- **`DataPaths.java` (Class):** Centralized configuration for all CSV file paths.

---

### 5) `com.motorph.service`
**Purpose:** Business logic layer for compliance, security, and feature execution.

- **`AuthService.java` (Class):** Authenticates users and resolves assigned roles.
- **`EmployeeService.java` (Class):** Handles employee CRUD operations.
- **`PayrollService.java` (Class):** Orchestrates salary computation flow.
- **`LeaveService.java` (Class):** Processes leave filing and supervisor approval logic.
- **`TimeService.java` (Class):** Manages clock-in and clock-out rules.
- **`LogService.java` (Class):** Unified logging engine used across services for traceability.

---

### 6) `com.motorph.service.strategy`
**Purpose:** Compliance layer using the Strategy Pattern to support regulatory updates.

- **`DeductionStrategy.java` (Interface):** Contract for deduction calculations.
- **`PayDeductionStrategy.java` (Class):** Concrete implementation for current tax and contribution rules.

---

### 7) `com.motorph.ui.swing`
**Purpose:** Swing UI layer organized into role-based dashboards and feature panels.

- **`LoginView.java` (JFrame Form):** Secure login screen that calls `AuthService`.
- **`MainDashboard.java` (JFrame Form):** Primary container that swaps views based on role and user actions.
- **`BasePanel.java` (Abstract Class):** Parent template for all feature panels.
- **`EmployeePanel.java` (JPanel Form):** Self-service for clock-in/out and leave requests.
- **`SupervisorPanel.java` (JPanel Form):** Team overview and leave approvals.
- **`HrPanel.java` (JPanel Form):** HR interface for employee management (CRUD).
- **`PayrollPanel.java` (JPanel Form):** Payroll processing UI and results display.

