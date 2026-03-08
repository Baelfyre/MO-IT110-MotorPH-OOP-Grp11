/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.motorph.domain.enums;

/**
 *
 * @author ACER
 */
public enum JobPosition {
    CHIEF_EXECUTIVE_OFFICER("Chief Executive Officer", true, true),
    CHIEF_OPERATING_OFFICER("Chief Operating Officer", true, true),
    CHIEF_FINANCE_OFFICER("Chief Finance Officer", true, true),
    CHIEF_MARKETING_OFFICER("Chief Marketing Officer", true, true),
    IT_OPERATIONS_AND_SYSTEMS("IT Operations and Systems", true, true),
    ACCOUNTING_HEAD("Accounting Head", true, true),
    HR_MANAGER("HR Manager", true, true),
    HR_TEAM_LEADER("HR Team Leader", true, true),
    HR_RANK_AND_FILE("HR Rank and File", false, false),
    ACCOUNT_MANAGER("Account Manager", true, true),
    ACCOUNT_TEAM_LEADER("Account Team Leader", true, true),
    ACCOUNT_RANK_AND_FILE("Account Rank and File", false, false),
    SALES("Sales", false, false),
    SUPPLY_CHAIN("Supply Chain", false, false),
    CUSTOMER_SERVICE("Customer Service", false, false);

    private final String label;
    private final boolean leadership;
    private final boolean supervisorEligible;

    JobPosition(String label, boolean leadership, boolean supervisorEligible) {
        this.label = label;
        this.leadership = leadership;
        this.supervisorEligible = supervisorEligible;
    }

    public String getLabel() {
        return label;
    }

    public boolean isLeadership() {
        return leadership;
    }

    public boolean isSupervisorEligible() {
        return supervisorEligible;
    }

    @Override
    public String toString() {
        return label;
    }

    public static JobPosition fromLabel(String label) {
        for (JobPosition value : values()) {
            if (value.label.equalsIgnoreCase(label)) {
                return value;
            }
        }
        return null;
    }
}
