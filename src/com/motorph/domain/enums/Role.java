/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.motorph.domain.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Role {

    EMPLOYEE("EMPLOYEE", Arrays.asList(
            "CAN_VIEW_SELF"
    )),
    HR("HR", Arrays.asList(
            "CAN_VIEW_ALL",
            "CAN_MANAGE_EMPLOYEES" // Explicitly allows CRUD, no Payroll access
    )),
    PAYROLL("PAYROLL", Arrays.asList(
            "CAN_VIEW_ALL",
            "CAN_PROCESS_PAYROLL",
            "CAN_GENERATE_PAYSLIP" // Cannot manage/delete employees
    )),
    SUPERVISOR("SUPERVISOR", Arrays.asList(
            "CAN_VIEW_ALL",
            "CAN_APPROVE_DTR",
            "CAN_APPROVE_LEAVE"
    )),
    IT("IT", Arrays.asList(
            "CAN_VIEW_ALL",
            "CAN_LOCK_ACCOUNTS",
            "CAN_RESET_PASSWORD"
    )),
    ADMIN("ADMIN", Arrays.asList( // Milestone requirement: Full Access
            "CAN_VIEW_ALL",
            "CAN_MANAGE_EMPLOYEES",
            "CAN_PROCESS_PAYROLL",
            "CAN_GENERATE_PAYSLIP",
            "CAN_APPROVE_DTR",
            "CAN_APPROVE_LEAVE",
            "CAN_LOCK_ACCOUNTS",
            "CAN_RESET_PASSWORD"
    ));

    private final String roleName;
    private final List<String> permissions;

    Role(String roleName, List<String> permissions) {
        this.roleName = roleName;
        this.permissions = permissions;
    }

    public String getRoleName() {
        return roleName;
    }

    public List<String> getPermissions() {
        return Collections.unmodifiableList(permissions);
    }
}
