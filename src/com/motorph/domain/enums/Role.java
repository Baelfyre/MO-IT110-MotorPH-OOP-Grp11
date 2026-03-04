/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.motorph.domain.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author OngoJ.
 */
public enum Role {

    EMPLOYEE("EMPLOYEE", Arrays.asList(
            "CAN_VIEW_SELF"
    )),
    HR("HR", Arrays.asList(
            "CAN_VIEW_ALL",
            "CAN_MANAGE_EMPLOYEES"
    )),
    PAYROLL("PAYROLL", Arrays.asList(
            "CAN_VIEW_ALL",
            "CAN_PROCESS_PAYROLL"
    )),
    SUPERVISOR("SUPERVISOR", Arrays.asList(
            "CAN_VIEW_ALL",
            "CAN_APPROVE_DTR"
    )),
    IT("IT", Arrays.asList(
            "CAN_VIEW_ALL",
            "CAN_LOCK_ACCOUNTS",
            "CAN_RESET_PASSWORD",
            "CAN_MANAGE_EMPLOYEES",
            "CAN_PROCESS_PAYROLL"
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
